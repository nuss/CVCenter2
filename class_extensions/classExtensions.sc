+Object {
	// execute .changed for the given array of keys, each denoting a function to be executed
	changedPerformKeys { |keys ... moreArgs|
		keys.do({ |key|
			this.changed(key, *moreArgs);
		})
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
			connector = widget.addOscConnector;
			index = connector.index;
		} {
			connector = widget.oscConnectors[index];
		};

		OscConnector.accum[widget] = widget.cv.input;
		learnFunc = { |msg, time, addr, recvPort|
			if (matching) {
				widget.wmc.oscConnections.m.value[index] = OSCFunc.newMatching(connector.prOSCFuncAction(widget.getOscMsgIndex(index)), msg[0], addr, port ? recvPort, argTemplate ?? { widget.getOscTemplate(index) }, dispatcher ?? { widget.getOscDispatcher(index) });
				"New matching OSCFunc created for OscConnector[%], listening to '%', msg index %, from NetAddr('%', %) on port %".format(
					index, msg[0], widget.getOscMsgIndex(index), addr.ip, addr.port, port ? recvPort
				).inform
			} {
				widget.wmc.oscConnections.m.value[index] = OSCFunc(connector.prOSCFuncAction(widget.getOscMsgIndex(index)), msg[0], addr, port ? recvPort, argTemplate ?? { widget.getOscTemplate(index) });
				"New OSCFunc created for OscConnector[%], listening to '%', msg index %, from NetAddr('%', %) on port %".format(
					index, msg[0], widget.getOscMsgIndex(index), addr.ip, addr.port, port ? recvPort
				).inform
			};
			widget.wmc.oscConnections.m.changedPerformKeys(widget.syncKeys, index);
			widget.wmc.oscDisplay.m.value[index].nameField = msg[0];
			widget.wmc.oscDisplay.m.value[index].connectorButVal = 1;
			widget.wmc.oscDisplay.m.value[index].connect = "disconnect";
			widget.wmc.oscDisplay.m.value[index].ipField = addr.ip.asSymbol;
			widget.wmc.oscDisplay.m.value[index].portField = addr.port;
			widget.wmc.oscDisplay.m.value[index].oscMatching = matching;
			widget.wmc.oscDisplay.m.value[index].template = widget.wmc.oscConnections.m.value[index].argTemplate;
			widget.wmc.oscDisplay.m.value[index].dispatcher = widget.wmc.oscConnections.m.value[index].dispatcher;
			widget.wmc.oscDisplay.m.value[index].learn = false;
			CVWidget.wmc.oscAddrAndCmds.m.value[addr.ip.asSymbol] ?? {
				CVWidget.wmc.oscAddrAndCmds.m.value.put(addr.ip.asSymbol, ())
			};
			if (CVWidget.wmc.oscAddrAndCmds.m.value[addr.ip.asSymbol][addr.port.asSymbol].isNil) {
				CVWidget.wmc.oscAddrAndCmds.m.value[addr.ip.asSymbol].put(addr.port.asSymbol, (msg[0] : msg[1..].size))
			} {
				CVWidget.wmc.oscAddrAndCmds.m.value[addr.ip.asSymbol][addr.port.asSymbol].put(msg[0], msg[1..].size)
			};
			CVWidget.wmc.oscAddrAndCmds.m.changedPerformKeys(CVWidget.syncKeys);
			widget.wmc.oscDisplay.m.changedPerformKeys(widget.syncKeys, index);
			thisProcess.removeOSCRecvFunc(learnFunc);
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
