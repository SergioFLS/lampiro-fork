package it.yup.util.storage;

import java.util.Enumeration;

public interface KeyStore {

	public abstract int[] getSizes();

	/**
	 * Store an item
	 * @param key
	 * @param data
	 */
	public abstract void store(byte key[], byte data[]);

	public abstract boolean hasKey(byte key[]);

	public abstract byte[] load(byte key[]);

	public abstract void delete(byte[] key);

	public abstract boolean close();

	public abstract Enumeration keys();

	public abstract boolean open();

	public abstract String getName();
}