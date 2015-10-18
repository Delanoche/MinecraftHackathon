package everything;

import com.sun.net.httpserver.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class MinecraftHackathon extends JavaPlugin implements Listener {

	Map<String, Location> players = new HashMap<String, Location>();

	@Override
	public void onEnable() {
		//		setupTcp();
		getServer().getPluginManager().registerEvents(this, this);
		try {
			printShit();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void printShit() throws FileNotFoundException {
		World world = Bukkit.getServer().getWorlds().get(0);
		for (int r = 0; r < 50; r++) {
			for (int s = 0; s < 50; s++) {
				JSONObject json = new JSONObject();
				JSONArray array = new JSONArray();
				for (int i = 100 * s; i < 100 * s + 100; i++) {
					for (int j = 100 * r; j < 100 * r + 100; j++) {
						JSONObject jObj = new JSONObject();
						Block block = world.getHighestBlockAt(i, j);
						block = world.getBlockAt(i, block.getY() - 1, j);
						Material material = block.getType();
						String name = material.toString();
						jObj.put("type", name);
						jObj.put("x", i);
						jObj.put("y", block.getY());
						jObj.put("z", j);
						array.add(jObj);
					}
				}
				json.put("blocks", array);
				final String obj = json.toJSONString();
				Runnable thread = new Runnable() {
					@Override
					public void run() {
						try {

							makePost("blocks", obj);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				};
				thread.run();
			}
		}
	}

	@Override
	public void onDisable() {

	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		// Get the player's location.
		//	    Location loc = event.getPlayer().getLocation();
		// Sets loc to five above where it used to be. Note that this doesn't change the player's position.
		//	    loc.setY(loc.getY() + 5);
		// Gets the block at the new location.
		//	    Block b = loc.getBlock();
		// Sets the block to type id 1 (stone).
		//	    b.setType(Material.STONE);
		Player player = event.getPlayer();
		String name = player.getName();
		if (players.get(name) == null || !players.get(name).equals(player.getLocation())) {
			players.put(name, player.getLocation());
			Location location = player.getLocation();
			final String json = "{\"name\": \"" + name + "\", \"x\": " + location.getBlockX() + ", \"y\": " + location.getBlockY() + ", \"z\": " + location.getBlockZ() + " }";
			getLogger().info(event.getPlayer().getLocation().toString());
			Runnable thread = new Runnable() {
				@Override
				public void run() {
					try {
						makePost("players", json);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			thread.run();
		}
	}

	public void makePost(String route, String json) throws IOException {
		String url="http://localhost:3000/" + route;
		URL object=new URL(url);

		HttpURLConnection con = (HttpURLConnection) object.openConnection();
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setRequestProperty("Content-Type", "application/json");
		con.setRequestProperty("Accept", "application/json");
		con.setRequestMethod("POST");
		OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
		wr.write(json);
		wr.flush();
		StringBuilder sb = new StringBuilder();  
		int HttpResult = con.getResponseCode(); 
		if(HttpResult == HttpURLConnection.HTTP_OK){
			BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(),"utf-8"));  
			String line = null;  
			while ((line = br.readLine()) != null) {  
				sb.append(line + "\n");  
			}  

			br.close();  

			getLogger().info(""+sb.toString());  

		}else{
			getLogger().info(con.getResponseMessage());  
		}  
	}

	public void setupTcp() {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				int portNumber = 8080;
				ServerSocket serverSocket;
				try {
					serverSocket = new ServerSocket(portNumber);
					while (true) {
						//						Socket clientSocket = serverSocket.accept();
						//						PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
						World world = Bukkit.getServer().getWorlds().get(0);
						Block block = world.getBlockAt(0, 0, 0);
						System.out.println(block);
						// BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		thread.run();
	}

	/*
	class MyHandler implements HttpHandler {

		@Override
		public void handle(HttpExchange exchange) throws IOException {
			String requestMethod = exchange.getRequestMethod();
			if (requestMethod.equalsIgnoreCase("GET")) {
				Headers responseHeaders = exchange.getResponseHeaders();
				responseHeaders.set("Content-type", "application/json");
				exchange.sendResponseHeaders(200, 0);

				Map<String, String> map = queryToMap(exchange.getRequestURI().getQuery());
				OutputStream responseBody = exchange.getResponseBody();
			}
		}

		public Map<String, String> queryToMap(String query){
			Map<String, String> result = new HashMap<String, String>();
			for (String param : query.split("&")) {
				String pair[] = param.split("=");
				if (pair.length>1) {
					result.put(pair[0], pair[1]);
				}else{
					result.put(pair[0], "");
				}
			}
			return result;
		}
	}
	 */

}
