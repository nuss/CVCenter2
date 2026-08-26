+Object {
	// execute .changed for the given array of keys, each denoting a function to be executed
	changedPerformKeys { |keys ... moreArgs|
		keys.do { |key|
			this.changed(key, *moreArgs);
		}
	}
}

+Dictionary {
	detect { |function|
		this.pairsDo { |key, val| if (function.value(val, key)) { ^val } };
		^nil;
	}

	detectKey { |function|
		this.pairsDo { |key, val| if (function.value(val, key)) { ^key } };
		^nil;
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

	depth {
		var depth = 0;
		var func = { |col|
			var cols = col.select { |it| it.isCollection };
			if (cols.size > 0) {
				depth = depth + 1;
				cols.do { |it|
					func.(it)
				}
			} { depth }
		};
		func.(this);
		^depth
	}
}

+OSCFunc {
	// usage in a CVWidget context only
	*cvWidgetLearn { |widget, slot, index, matching(false), port, argTemplate, dispatcher|
		var learnFunc, connector;
		var oscConnectors, connectionsModel, displayModel;

		if (widget.isNil or: { widget.isKindOf(CVWidget).not}) {
			"Cannot connect non-existing or invalid widget".error;
			^nil
		};

		switch (widget.class)
		{ CVWidgetKnob } {
			oscConnectors = widget.oscConnectors;
			connectionsModel = widget.wmc.oscConnections.m;
			displayModel = widget.wmc.oscDisplay.m;
		}
		{ CVWidgetMS } {
			oscConnectors = widget.oscConnectors[slot];
			connectionsModel = widget.wmc.oscConnections.m[slot];
			displayModel = widget.wmc.oscDisplay.m[slot];
		};

		if (index.isNil or: {
			oscConnectors[index].isNil or: {
				connectionsModel.value[index].notNil
			}
		}) {
			connector = widget.addOscConnector;
			index = connector.index;
		} {
			connector = oscConnectors[index];
		};

		OscConnector.accum[widget] = widget.cv.input;
		learnFunc = { |msg, time, addr, recvPort|
			if (matching) {
				connectionsModel.value[index] = OSCFunc.newMatching(connector.prOSCFuncAction(widget.getOscMsgIndex(index, slot)), msg[0], addr, port ? recvPort, argTemplate ?? { widget.getOscTemplate(index, slot) }, dispatcher ?? { widget.getOscDispatcher(index, slot) });
				"New matching OSCFunc created for OscConnector[%], listening to '%', msg index %, from NetAddr('%', %) on port %".format(
					index, msg[0], widget.getOscMsgIndex(index, slot), addr.ip, addr.port, port ? recvPort
				).inform
			} {
				connectionsModel.value[index] = OSCFunc(connector.prOSCFuncAction(widget.getOscMsgIndex(index, slot)), msg[0], addr, port ? recvPort, argTemplate ?? { widget.getOscTemplate(index, slot) });
				"New OSCFunc created for OscConnector[%], listening to '%', msg index %, from NetAddr('%', %) on port %".format(
					index, msg[0], widget.getOscMsgIndex(index, slot), addr.ip, addr.port, port ? recvPort
				).inform
			};
			connectionsModel.changedPerformKeys(widget.syncKeys, index);
			displayModel.value[index].nameField = msg[0];
			// displayModel.value[index].connectorButVal = 1;
			// displayModel.value[index].connect = "disconnect";
			displayModel.value[index].ipField = addr.ip.asSymbol;
			displayModel.value[index].portField = addr.port;
			displayModel.value[index].oscMatching = matching;
			connectionsModel.value[index].argTemplate !? {
				displayModel.value[index].template = connectionsModel.value[index].argTemplate.cs
			};
			// connectionsModel.value[index].dispatcher !? {
			// 	displayModel.value[index].dispatcher = connectionsModel.value[index].dispatcher
			// };
			displayModel.value[index].learn = false;
			displayModel.value[index].connectState = ["disconnect", Color.white, Color.red];
			displayModel.value[index].connectEnabled = true;
			CVWidget.wmc.oscAddrAndCmds.m.value[addr.ip.asSymbol] ?? {
				CVWidget.wmc.oscAddrAndCmds.m.value.put(addr.ip.asSymbol, ())
			};
			if (CVWidget.wmc.oscAddrAndCmds.m.value[addr.ip.asSymbol][addr.port.asSymbol].isNil) {
				CVWidget.wmc.oscAddrAndCmds.m.value[addr.ip.asSymbol].put(addr.port.asSymbol, (msg[0] : msg[1..].size))
			} {
				CVWidget.wmc.oscAddrAndCmds.m.value[addr.ip.asSymbol][addr.port.asSymbol].put(msg[0], msg[1..].size)
			};
			CVWidget.wmc.oscAddrAndCmds.m.changedPerformKeys(CVWidget.syncKeys);
			displayModel.changedPerformKeys(widget.syncKeys, index);
			thisProcess.removeOSCRecvFunc(learnFunc);
		};
		// either collect or learn - we've decided to learn'
		OSCCommands.collectSync(false);
		// widget.wmc.oscDisplay.m.value[index].disconnect = "cancel";
		// widget.wmc.oscConnections.m.changedPerformKeys(widget.syncKeys, index);

		thisProcess.addOSCRecvFunc(learnFunc);
		displayModel.value[index].connectState = ["waiting...", Color.white, Color.gray];
		displayModel.value[index].connectEnabled = false;
		displayModel.changedPerformKeys(widget.syncKeys, index);
	}
}

+MIDIFunc {
	// will only work in a CVWidget context
	learnSync { |widget, slot, index, learnVal = false|
		var learnFunc;
		learnFunc = this.learnFuncSync(widget, slot, index, learnVal);
		this.disable;
		this.init(learnFunc); // keep old args if specified, so we can learn from particular channels, srcs, etc.
	}

	// cc only for now
	learnFuncSync { |widget, slot, index, learnVal|
		var oldFunc, learnFunc;
		var m =	switch (widget.class)
		{ CVWidgetKnob } { widget.wmc.midiDisplay.m }
		{ CVWidgetMS } { widget.wmc.midiDisplay.m[slot] };

		oldFunc = func;
		if (msgType === \control) {
			^{ |val, num, chan, srcID|
				"MIDIFunc learned: type: %\tnum: %\tval: %\tchan: %\tsrcID: %\t\n".postf(msgType, num, val.postln, chan, srcID);
				this.disable;
				this.remove(learnFunc);
				oldFunc.value(val, num, chan, srcID);// do first action
				this.init(oldFunc, num, chan, msgType, srcID, if(learnVal, val, nil));
				m.value[index].src = srcID;
				m.value[index].chan = chan;
				m.value[index].ctrl = num;
				if (learnVal) {
					m.value[index].template = val
				} {
					m.value[index].template = nil
				};
				m.changedPerformKeys(widget.syncKeys, index);
			}
		}
	}
}
