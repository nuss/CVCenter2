+Object {

	// execute .changed for the given array of keys, each denoting a function to be executed
	changedPerformKeys { |keys ... moreArgs|
		keys.do({ |key|
			this.changed(key, *moreArgs);
		})
	}

}

+Env {

	multiChannelExpands { |env|
		var test = { |arr| arr.flat.size > arr.numChannels };
	}

}