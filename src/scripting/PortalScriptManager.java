/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc> 
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License version 3
 as published by the Free Software Foundation. You may not use, modify
 or distribute this program under any other version of the
 GNU Affero General Public License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package scripting;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import client.MapleClient;
import constants.ServerConfig;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import javax.script.ScriptException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.MaplePortal;
import tools.EncodingDetect;
import tools.FileoutputUtil;

public class PortalScriptManager {

    private static final PortalScriptManager instance = new PortalScriptManager();
    private final Map<String, PortalScript> scripts = new HashMap<>();
    private final static ScriptEngineFactory sef = new ScriptEngineManager().getEngineByName("nashorn").getFactory();
    private static final Logger logger = LogManager.getLogger(PortalScriptManager.class);

    public static PortalScriptManager getInstance() {
        return instance;
    }

    private PortalScript getPortalScript(final String scriptName) {
        if (scripts.containsKey(scriptName)) {
            return scripts.get(scriptName);
        }

        final File scriptFile = new File("scripts/portal/" + scriptName + ".js");
        if (!scriptFile.exists()) {
            return null;
        }

        InputStreamReader fr = null;
        final ScriptEngine portal = sef.getScriptEngine();
        try {
            fr = new InputStreamReader(new FileInputStream(scriptFile), EncodingDetect.getJavaEncode(scriptFile));
            CompiledScript compiled = ((Compilable) portal).compile(fr);
            compiled.eval();
        } catch (FileNotFoundException | UnsupportedEncodingException | ScriptException e) {
            System.err.println("Error executing Portalscript: " + scriptName + ":" + e);
            FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Error executing Portal script. (" + scriptName + ") " + e);
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (final IOException e) {
                    System.err.println("ERROR CLOSING" + e);
                    FileoutputUtil.outputFileError(FileoutputUtil.ScriptEx_Log, e);
                }
            }
        }
        final PortalScript script = ((Invocable) portal).getInterface(PortalScript.class);
        scripts.put(scriptName, script);
        return script;
    }

    public final void executePortalScript(final MaplePortal portal, final MapleClient c) {
        final PortalScript script = getPortalScript(portal.getScriptName());

        if (script != null) {
            try {
                if (c.getPlayer().isAdmin() || ServerConfig.logPackets) {
                    c.getPlayer().dropMessage(6, "执行传送点脚本名为:(" + portal.getScriptName() + ".js)的文件 在地图 " + c.getPlayer().getMapId() + " - " + c.getPlayer().getMap().getMapName());
                }
                script.enter(new PortalPlayerInteraction(c, portal));
            } catch (Exception e) {
                if (c.getPlayer().isAdmin() || ServerConfig.logPackets) {
                    c.getPlayer().dropMessage(6, "执行地图脚本过程中发生错误.请检查传送点脚本名为:( " + portal.getScriptName() + ".js)的文件，错误信息：" + e);
                }
                System.err.println("Error entering Portalscript: " + portal.getScriptName() + " : " + e);
                FileoutputUtil.outputFileError(FileoutputUtil.ScriptEx_Log, e);
            }
        } else {
            if (c.getPlayer().isAdmin() || ServerConfig.logPackets) {
                c.getPlayer().dropMessage(6, "未找到传送点脚本名为:(" + portal.getScriptName() + ".js)的文件 在地图 " + c.getPlayer().getMapId() + " - " + c.getPlayer().getMap().getMapName());
            }
            logger.warn("Unhandled portal script " + portal.getScriptName() + " on map " + c.getPlayer().getMapId());
            FileoutputUtil.log("logs/Log_Script_UnhandledPortal.txt", "Unhandled portal script " + portal.getScriptName() + " on map " + c.getPlayer().getMapId());
        }
    }

    public final void clearScripts() {
        scripts.clear();
    }
}
