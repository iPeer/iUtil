package com.ipeer.iutil.remote.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import com.ipeer.iutil.engine.Engine;

public class iUtilAccount {

	private String password, username;
	private boolean isAdmin;
	
	public iUtilAccount(String password, String username, boolean isAdmin) throws IOException, UserAlreadyExistsException, CannotSavePasswordException {
		this.password = password;
		this.username = username;
		this.isAdmin = isAdmin;
		saveData();
	}
	
	public void saveData() throws IOException, UserAlreadyExistsException, CannotSavePasswordException {
		saveData(false);
	}
	
	public void saveData(boolean b) throws IOException, UserAlreadyExistsException, CannotSavePasswordException {
		File a = new File(Engine.iUtilAccountsDir, this.username+".iaf");
		if (a.exists() && !b)
			throw new UserAlreadyExistsException();
		DataOutputStream out = new DataOutputStream(new FileOutputStream(a));
		KeyGenerator kg;
		Key key;
		Cipher ci;
		try {
			kg = KeyGenerator.getInstance("AES");
			kg.init(128);
			key = kg.generateKey();
			out.writeInt(key.getEncoded().length);
			ci = Cipher.getInstance("AES/ECB/PKCS5Padding");
			ci.init(Cipher.ENCRYPT_MODE, key);
			out.write(key.getEncoded());
			byte[] pass = ci.doFinal(password.getBytes());
			out.writeInt(pass.length);
			out.write(pass);
			byte[] user = ci.doFinal(username.getBytes());
			out.writeInt(user.length);
			out.write(user);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			out.close();
			throw new CannotSavePasswordException();
		} 
		out.writeBoolean(isAdmin);
		out.close();
	}

	public iUtilAccount (String username) throws NoSuchAccountException, IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		this.username = username;
		loadData();
	}

	private void loadData() throws NoSuchAccountException, IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		File a = new File(Engine.iUtilAccountsDir, this.username+".iaf");
		Key key;
		Cipher ci = Cipher.getInstance("AES/ECB/PKCS5Padding");;
		DataInputStream in = new DataInputStream(new FileInputStream(a));
		try {
			byte[] ke = new byte[in.readInt()];
			in.readFully(ke);
			key = new SecretKeySpec(ke, "AES");
			ci.init(Cipher.DECRYPT_MODE, key);
			byte[] pass = new byte[in.readInt()];
			in.readFully(pass);
			this.password = new String(ci.doFinal(pass));
			byte[] user = new byte[in.readInt()];
			in.readFully(user);
			this.username = new String(ci.doFinal(user));
			this.isAdmin = in.readBoolean();
			in.close();
		} catch (FileNotFoundException e) {
			in.close();
			throw new NoSuchAccountException();
		}
		catch (Exception e) {
			in.close();
			throw new IOException();
		}
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public boolean isAdmin() {
		return this.isAdmin;
	}
	
	public String getPassword() {
			return this.password;
	}

	public void setAdmin(boolean b) {
		this.isAdmin = b;
	}
	
}
