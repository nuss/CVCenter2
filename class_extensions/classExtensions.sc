+Object {

	// execute .changed for the given array of keys, each denoting a function to be executed
	changedPerformKeys { |keys ... moreArgs|
		keys.do({ |key|
			this.changed(key, *moreArgs);
		})
	}

}

+Font {

        *available { |...names|
                var match;
                names.do { |name|
                        match = Font.availableFonts.detect(_ == name);
                        match !? {
                                ^match
                        }
                }
                ^nil;
        }

}
