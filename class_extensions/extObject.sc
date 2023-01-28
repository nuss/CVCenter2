+Object {

	// execute .changed for the given array of symbols
	changedKeys { |keys ... moreArgs|
		// "keys: %".format(keys).postln;
		keys.do({ |key|
			this.changed(key.asSymbol, *moreArgs);
			// "key: %, moreArgs: %".format(key, moreArgs).postln;
		})
	}

}