package dk.mineclub.minecore.api;

public class MineCoreApi {
	private static MineCoreApi instance;

	public static MineCoreApi getInstance() {
		if (instance == null) {
			instance = new MineCoreApi();
		}
		return instance;
	}

	private MineCoreApi() {

	}
}
