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
	// usage in a CVWidget context only
	*cvWidgetLearn { |widget, index, matching=false, port, argTemplate, dispatcher|
		var learnFunc, connector;

		if (widget.isNil or: { widget.isKindOf(CVWidget).not}) {
			"Cannot connect non-existing or invalid widget".error;
			^nil
		};

		if (index.isNil or: {
			widget.oscConnectors[index].isNil or: {
				widget.wmc.oscConnections.m.value[index].notNil
			}
		}) {
			connector = widget.addOscConnector/*.postln*/;
			index = connector.index/*.postln*/;
			// widget.wmc.oscConnections.m.value.postln;
		} {
			connector = widget.oscConnectors[index];
		};

		// "connector: %, index: %".format(connector, index).postln;

		OscConnector.accum[widget] = widget.cv.input;
		learnFunc = { |msg, time, addr, recvPort|
			// "connector: %".format(connector).postln;
			if (matching) {
				widget.wmc.oscConnections.m.value[index] = OSCFunc.newMatching(connector.prOSCFuncAction, msg[0], addr, port ? recvPort, argTemplate ?? { widget.getOscTemplate(index) }, dispatcher ?? { widget.getOscDispatcher(index) });
				"New matching OSCFunc created for OscConnector[%], listening to '%' from NetAddr('%', %) on port %".format(index, msg[0], addr.ip, addr.port, port ? recvPort).inform;
			} {
				// widget.wmc.oscConnections.m.value.postln;
				widget.wmc.oscConnections.m.value[index] = OSCFunc(connector.prOSCFuncAction, msg[0], addr, port ? recvPort, argTemplate ?? { widget.getOscTemplate(index) });
				"New OSCFunc created for OscConnector[%], listening to '%' from NetAddr('%', %) on port %".format(index, msg[0], addr.ip, addr.port, port ? recvPort).inform;
			};
			widget.wmc.oscConnections.m.changedPerformKeys(widget.syncKeys, index);
			thisProcess.removeOSCRecvFunc(learnFunc)
		};
		// either collect or learn - we've decided to learn'
		OSCCommands.collectSync(false);
		thisProcess.addOSCRecvFunc(learnFunc);
	}
}

+MIDIFunc {
	// will only work in a CVWidget context
	learnSync { |widget, index, learnVal=false|
		var learnFunc;
		learnFunc = this.learnFuncSync(widget, index, learnVal);
		this.disable;
		this.init(learnFunc); // keep old args if specified, so we can learn from particular channels, srcs, etc.
	}

	// cc only for now
	learnFuncSync { |widget, index, learnVal|
		var oldFunc, learnFunc;

		oldFunc = func;
		if (msgType === \control) {
			^{ |val, num, chan, srcID|
				"MIDIFunc learned: type: %\tnum: %\tval: %\tchan: %\tsrcID: %\t\n".postf(msgType, num, val, chan, srcID);
				this.disable;
				this.remove(learnFunc);
				oldFunc.value(val, num, chan, srcID);// do first action
				this.init(oldFunc, num, chan, msgType, srcID, if(learnVal, val, nil));
				widget.wmc.midiDisplay.m.value[index].src = srcID;
				widget.wmc.midiDisplay.m.value[index].chan = chan;
				widget.wmc.midiDisplay.m.value[index].ctrl = num;
				if (learnVal) {
					widget.wmc.midiDisplay.m.value[index].templ = val
				} {
					widget.wmc.midiDisplay.m.value[index].templ = nil
				};
				widget.wmc.midiDisplay.m.changedPerformKeys(widget.syncKeys, index);
			}
		}
	}
}
