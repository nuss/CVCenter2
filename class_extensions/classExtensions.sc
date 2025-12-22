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

+OSCCommands {

	*collectSync { |play = true|
		if (play) {
			if (collecting == false) {
				thisProcess.addOSCRecvFunc(oscFunc);
				CmdPeriod.add({ this.collectSync(false) });
				CVWidget.wmc.isScanningOsc.m.value_(true).changedPerformKeys(CVWidget.syncKeys);
				collecting = true;
				"collecting OSC commands started".inform;
			}
		} {
			thisProcess.removeOSCRecvFunc(oscFunc);
			CmdPeriod.remove({ this.collectSync(false) });
			CVWidget.wmc.oscAddrAndCmds.m.changedPerformKeys(CVWidget.syncKeys);
			CVWidget.wmc.isScanningOsc.m.value_(false).changedPerformKeys(CVWidget.syncKeys);
			collecting = false;
			"collecting OSC commands stopped".inform;
		}
	}

}

+Collection {

	includesAllEqual { |aCollection|
		aCollection.do { |item| if (this.includesEqual(item).not) { ^false }};
		^true
	}

	includesNone { |aCollection|
		aCollection.do { |item| if (this.includes(item)) { ^false }};
		^true
	}

	includesNoneEqual { |aCollection|
		aCollection.do { |item| if (this.includesEqual(item)) { ^false }};
		^true
	}

}

+OSCFunc {

	*cvWidgetlearn { |matching=false, slot=1, port|
		var funcType;
		var learnFunc = { |msg, time, addr, recvPort|
			if (matching) {
				OSCFunc.newMatching({ |m, t, a, rp| /* do something */}, msg[0], addr, port ? recvPort)
			} {
				OSCFunc({ |m, t, a, rp| /* do something */}, msg[0], addr, port ? recvPort)
			};
			thisProcess.removeOSCRecvFunc(thisFunction)
		};
		// either collect or learn - we've decided to learn'
		OSCCommands.collectSync(false);
		^thisProcess.addOSCRecvFunc(learnFunc)
	}

}

