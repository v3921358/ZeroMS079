package client;

import client.inventory.Equip;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import database.DBConPool;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.MapleItemInformationProvider;
import server.quest.MapleQuest;
import tools.FileoutputUtil;
import tools.Pair;
import tools.Triple;
import tools.data.MaplePacketLittleEndianWriter;
import tools.packet.CField;
import tools.packet.CWvsContext;

public final class MonsterBook implements Serializable {

    private static final long serialVersionUID = 7179541993413738569L;
    private boolean changed = false;
    private int SpecialCard = 0, NormalCard = 0, BookLevel = 1;
    private final Map<Integer, Integer> cards;
    private static final Logger logger = LogManager.getLogger(MonsterBook.class);

    public MonsterBook(Map<Integer, Integer> cards, MapleCharacter chr) {
        this.cards = cards;

        for (Map.Entry<Integer, Integer> card : cards.entrySet()) {
            if (GameConstants.isSpecialCard(card.getKey())) {

                SpecialCard += card.getValue();
            } else {
                NormalCard += card.getValue();
            }
        }
        calculateLevel();
    }

    public Map<Integer, Integer> getCards() {
        return cards;
    }

    public final int getTotalCards() {
        return SpecialCard + NormalCard;
    }

    public final int getLevelByCard(final int cardid) {
        return cards.get(cardid) == null ? 0 : cards.get(cardid);
    }

    public static final MonsterBook loadCards(int charid, MapleCharacter chr) throws SQLException {
        Map<Integer, Integer> cards;
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM monsterbook WHERE charid = ? ORDER BY cardid ASC")) {
                ps.setInt(1, charid);
                try (ResultSet rs = ps.executeQuery()) {
                    cards = new LinkedHashMap();
                    while (rs.next()) {
                        cards.put(rs.getInt("cardid"), rs.getInt("level"));
                    }
                }
            }
            return new MonsterBook(cards, chr);
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.MysqlEx_Log, ex);
            System.err.println(ex);
        }
        return null;
    }

    public final void saveCards(int charid, Connection con) throws SQLException {
        if (!this.changed) {
            return;
        }
        try {
            PreparedStatement ps = con.prepareStatement("DELETE FROM monsterbook WHERE charid = ?");
            ps.setInt(1, charid);
            ps.execute();
            ps.close();
            this.changed = false;
            if (this.cards.isEmpty()) {
                return;
            }

            boolean first = true;
            StringBuilder query = new StringBuilder();

            for (Map.Entry all : this.cards.entrySet()) {
                if (first) {
                    first = false;
                    query.append("INSERT INTO monsterbook VALUES (DEFAULT,");
                } else {
                    query.append(",(DEFAULT,");
                }
                query.append(charid);
                query.append(",");
                query.append(all.getKey());
                query.append(",");
                query.append(all.getValue());
                query.append(")");
            }
            ps = con.prepareStatement(query.toString());
            ps.execute();
            ps.close();
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.MysqlEx_Log, ex);
            System.err.println(ex);
        }
    }

    private void calculateLevel() {
        int Size = NormalCard + SpecialCard;
        BookLevel = 8;

        for (int i = 0; i < 8; i++) {
            if (Size <= GameConstants.getBookLevel(i)) {
                BookLevel = (i + 1);
                break;
            }
        }
    }

    public final void addCardPacket(final MaplePacketLittleEndianWriter mplew) {
        mplew.writeShort(cards.size());

        for (Map.Entry<Integer, Integer> all : cards.entrySet()) {
            mplew.writeShort(GameConstants.getCardShortId(all.getKey())); // Id
            mplew.write(all.getValue()); // Level
        }
    }

    public final void addCharInfoPacket(final int bookcover, final MaplePacketLittleEndianWriter mplew) {
        mplew.writeInt(BookLevel);
        mplew.writeInt(NormalCard);
        mplew.writeInt(SpecialCard);
        mplew.writeInt(NormalCard + SpecialCard);
        mplew.writeInt(MapleItemInformationProvider.getInstance().getCardMobId(bookcover));
    }

    public final void updateCard(final MapleClient c, final int cardid) {
        c.getSession().write(CWvsContext.changeCover(cardid));
    }

    public final void addCard(final MapleClient c, final int cardid) {
        changed = true;

        c.getPlayer().getMap().broadcastMessage(c.getPlayer(), CField.EffectPacket.showForeignEffect(c.getPlayer().getId(), 0xF), false);

        if (cards.containsKey(cardid)) {
            final int levels = cards.get(cardid);
            if (levels >= 5) {
                c.getSession().write(CWvsContext.addCard(true, cardid, levels));
            } else {
                if (GameConstants.isSpecialCard(cardid)) {
                    SpecialCard += 1;
                } else {
                    NormalCard += 1;
                }
                c.getSession().write(CWvsContext.addCard(false, cardid, levels));
                c.getSession().write(CWvsContext.InfoPacket.showGainCard(cardid));
                c.getSession().write(CField.EffectPacket.showForeignEffect(0xF));
                cards.put(cardid, levels + 1);
                calculateLevel();
            }
            return;
        }
        if (GameConstants.isSpecialCard(cardid)) {
            SpecialCard += 1;
        } else {
            NormalCard += 1;
        }
        // New card
        cards.put(cardid, 1);
        c.getSession().write(CWvsContext.addCard(false, cardid, 1));
        c.getSession().write(CWvsContext.InfoPacket.showGainCard(cardid));
        c.getSession().write(CField.EffectPacket.showForeignEffect(0xF));
        calculateLevel();
    }
}
