package com.ipeer.iutil.awesome.enderchests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ipeer.iutil.engine.Engine;
import com.ipeer.iutil.engine.Utils;

public class Enderchests {
	
	protected Engine engine;
	private List<Enderchest> enderchests;
	
	public Enderchests(Engine engine) {
		this.engine = engine;
		enderchests = loadChests();
	}
	
	public static void main(String[] args) {
		Enderchests e = new Enderchests(null);
		Enderchest e2 = new Enderchest("pink", "darkgray", "lightgray", "iPeer", "Tests");
		try {
			e.registerChest(e2);
			System.err.println("--> "+e.saveChests());
		} catch (EnderchestExistsException e1) {
			System.err.println(e1.getMessage());
		}
	}
	
	public List<Enderchest> getChestList() {
		return this.enderchests;
	}
	
	public void registerChest(Enderchest e) throws EnderchestExistsException {
		String colourCheck = Utils.intArrayToString(e.getColours());
		Iterator it = enderchests.iterator();
		while (it.hasNext()) {
			Enderchest ec = (Enderchest)it.next();
			if (e.getUsage().equals(ec.getUsage()) || colourCheck.equals(Utils.intArrayToString(ec.getColours())))
					throw new EnderchestExistsException("That enderchest has already been registered by "+ec.getRegistrar());
		}
		enderchests.add(e);
	}
	
	public String saveChests() {
		
		File file = Engine.AWeSomeEnderchests;
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			Iterator it = enderchests.iterator();
			while (it.hasNext()) {
				Enderchest e = (Enderchest)it.next();
				String write = e.getC1()+"\01"+e.getC2()+"\01"+e.getC3()+"\01"+e.getRegistrar()+"\01"+e.getUsage();
				out.write(write+"\n");
			}
			out.close();
			return "Your Enderchest has been registered.";
		} 
		catch (Exception e) {
			return "Could not register chest! "+e.toString()+"//"+e.getStackTrace()[0].toString();
		}
		
		
	}
	
	private List<Enderchest> loadChests() {
		File file = Engine.AWeSomeEnderchests;
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			List<Enderchest> ret = new ArrayList<Enderchest>();
			String line = "";
			while ((line = in.readLine()) != null) {
				String[] data = line.split("\01");
				Enderchest e = new Enderchest(Integer.valueOf(data[0]), Integer.valueOf(data[1]), Integer.valueOf(data[2]), data[3].trim(), data[4]);
				ret.add(e);
			}
			in.close();
			return ret;
		} 
		catch (IOException e) {
			return new ArrayList<Enderchest>();
		}
		
	}

	
	
	
}
