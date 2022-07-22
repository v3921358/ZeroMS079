package client;

import java.io.Serializable;

public class SkillEntry implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    public int skillevel;
    public byte masterlevel;
    public long expiration;
    public int teachId;
    public byte position;

    public SkillEntry(final int skillevel, final byte masterlevel, final long expiration) {
        this.skillevel = skillevel;
        this.masterlevel = masterlevel;
        this.expiration = expiration;
        this.teachId = 0;
        this.position = -1;
    }

    public SkillEntry(int skillevel, byte masterlevel, long expiration, int teachId) {
        this.skillevel = skillevel;
        this.masterlevel = masterlevel;
        this.expiration = expiration;
        this.teachId = teachId;
        this.position = -1;
    }

    public SkillEntry(int skillevel, byte masterlevel, long expiration, int teachId, byte position) {
        this.skillevel = skillevel;
        this.masterlevel = masterlevel;
        this.expiration = expiration;
        this.teachId = teachId;
        this.position = position;
    }
}
