
import com.sun.net.httpserver.HttpServer;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import javax.swing.*;
import java.awt.*;

/** Simple program showing the minimal Java program to launch a script.
 * 
 * <p> In typical usage, a Globals object must be created to hold global state, 
 * and it can be used to compile and load lua files into executable LuaValue
 * instances.
 * 
 * @see Globals
 * @see LuaValue
 */
public class SampleJseMain {
	
	public static void main(String[] args) throws Exception {
		//String script = "examples/lua/swingapp.lua";
		//String script = "examples/lua/hello.lua";
		String script = "examples/lua/testapp.lua";

		//new javax.swing.JTextArea().setForeground(Color.WHITE);
		// create an environment to run in
		Globals globals = JsePlatform.standardGlobals();
		// Use the convenience function on Globals to load a chunk.
		LuaValue chunk = globals.loadfile(script);
		//new javax.swing.JMenuItem().addActionListener();
		// Use any of the "call()" or "invoke()" functions directly on the chunk.
		chunk.call( LuaValue.valueOf(script) );
	}


}
