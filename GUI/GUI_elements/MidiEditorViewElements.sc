// MIDI editors

MidiConnectorNameField : ConnectorElementView {
	classvar <all;
	var <connector, <widget;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, connectorID=0|
		^super.new.init(parent, widget, rect, connectorID);
	}

	init { |parentView, wdgt, rect, index|
		all[wdgt] ?? { all[wdgt] = List[] };
		all[wdgt].add(this);

		widget = wdgt;
		mc = widget.wmc.midiConnectorNames;
		this.view = TextField(parentView, rect);
		this.index_(index);
		this.view.action_({ |tf|
			this.connector.name_(tf.string.asSymbol)
		});
		this.view.onClose_({ this.close });
		this.prAddController;
	}

	index_ { |connectorID|
		connector = widget.midiConnectors[connectorID];
		mc.model.value !? {
			this.view.string_(mc.model.value[connectorID])
		}
	}

	prAddController {
		var conID;
		mc.controller ?? {
			mc.controller = SimpleController(mc.model)
		};
		syncKey = \midiConnectorName;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				conID = moreArgs[0];
				all[widget].do { |tf|
					if (tf.connector === widget.midiConnectors[conID]) {
						tf.view.string_(changer.value[conID]);
					}
				};
			})
		}
	}
}

MidiConnectorSelect : ConnectorElementView {
	classvar <all;
	var <connector, <widget;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, connectorID=0|
		^super.new.init(parent, widget, rect, connectorID);
	}

	init { |parentView, wdgt, rect, index|
		all[wdgt] ?? { all[wdgt] = List[] };
		all[wdgt].add(this);

		widget = wdgt;
		mc = widget.wmc.midiConnectorNames;
		this.view = PopUpMenu(parentView)
		.items_(widget.midiConnectors.collect(_.name) ++ ["add MidiConnector..."]);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.prAddController;
	}

	index_ { |connectorID|
		connector = widget.midiConnectors[connectorID];
		this.view.value_(connectorID);
	}

	prAddController {
		var items, conID;
		var curValue;
		mc.controller ?? {
			mc.controller = SimpleController(mc.model)
		};
		syncKey = \midiConnectorSelect;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				conID = moreArgs[0];
				all[widget].do { |sel, i|
					items = sel.view.items;
					items[conID] = changer.value[conID];
					curValue = sel.view.value;
					sel.view.items_(items).value_(curValue);
					if (sel.connector === widget.midiConnectors[conID]) {
						sel.view.value_(conID)
					}
				}
			})
		}
	}

}

// Elements must not hold a fixed ID as connectors can get deleted from
// the widget's oscConnectors / midiConnectors lists. Hence, rather determine
// the current index from querying the widget's oscConnectors / midiConnectors list.

MidiLearnButton : ConnectorElementView {
	classvar <all;
	// widget must be a getter as it's called
	// in close(), defined in ConnectorElementView
	var <connector, <widget;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, connectorID=0|
		^super.new.init(parent, widget, rect, connectorID);
	}

	init { |parentView, wdgt, rect, index|
		var defaultState;
		widget = wdgt;
		all[widget] ?? { all.put(widget, List[]) };
		all[widget].add(this);

		mc = widget.wmc.midiDisplay;
		if (mc.model.value[index].learn == "C") {
			defaultState = [mc.model.value[index].learn, Color.black, Color.green];
			mc.model.value[index].toolTip = "Connect using selected parameters";
		} {
			defaultState = ["L", Color.white, Color.blue];
		};
		this.view = Button(parentView, rect).states_([
			defaultState,
			["X", Color.white, Color.red]
		]).maxWidth_(25).toolTip_(mc.model.value[index].toolTip);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |bt|
			var i = widget.midiConnectors.indexOf(this.connector);
			var src, chan, ctrl;
			mc.model.value[i].learn = bt.states[bt.value][0];
			mc.model.changedPerformKeys(widget.syncKeys, i);
			if (mc.model.value[i].learn == "X") {
				if (mc.model.value[i].src != "source...") { src = mc.model.value[i].src };
				if (mc.model.value[i].chan != "chan") { chan = mc.model.value[i].chan };
				if (mc.model.value[i].ctrl != "ctrl") { ctrl = mc.model.value[i].ctrl };
				widget.midiConnect(connector, src, chan, ctrl);
				if (src.notNil or: { chan.notNil or: { ctrl.notNil }}) {
					all[widget].do { |b|
						if (widget.midiConnectors.indexOf(b.connector.postln) == i) {
							b.view.states_([
								["L", Color.white, Color.blue],
								["X", Color.white, Color.red]
							]).value_(1).toolTip_(mc.model.value[i].toolTip)
						}
					}
				}
			}
			{
				widget.midiDisconnect(connector);
				all[widget].do { |b|
					if (widget.midiConnectors.indexOf(b.connector.postln) == i) {
						b.view.toolTip_(mc.model.value[i].toolTip);
					}
				}
			}
		});
		this.prAddController;
	}

	// the connector's ID will be dynamic and change
	// any time a connector with a lower ID in the widget's
	// midiConnectors list gets deleted!!!
	index_ { |connectorID|
		// we need the connector, not its current ID in widget.midiConnectors
		widget.midiConnectors[connectorID] !? {
			connector = widget.midiConnectors[connectorID];
			mc.model.value[connectorID] !? {
				mc.model.value[connectorID].learn.switch(
					"X", {
						this.view.value_(1)
					},
					"L", {
						this.view.value_(0)
					}
				)
			}
		}
	}

	prAddController {
		var pos, conID;
		mc.controller ?? {
			mc.controller = SimpleController(mc.model)
		};
		syncKey = \midiLearnButton;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true);
			// the following is global for all MidiLearnButtons
			// there must be no notion of 'this' as all MidiLearnButton instances are affected
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				conID = moreArgs[0];
				all[widget].do { |but, i|
					if (but.connector === widget.midiConnectors[conID]) {
						if (changer.value[conID].learn == "C") {
							// mc.model.value[i].toolTip = "Connect using selected parameters";
							but.view.states_([
								["C", Color.black, Color.green],
								["X", Color.white, Color.red]
							])
						};
						pos = but.view.states.detectIndex { |state, j|
							state[0] == changer.value[conID].learn
						};
						defer { but.view.value_(pos).toolTip_(mc.model.value[conID].toolTip) }
					}
				}
			})
		}
	}
}

MidiSrcSelect : ConnectorElementView {
	classvar <all;
	var <connector, <widget;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, connectorID = 0|
		^super.new.init(parent, widget, rect, connectorID);
	}

	init { |parentView, wdgt, rect, index|
		all[wdgt] ?? { all[wdgt] = List[] };
		all[wdgt].add(this);

		widget = wdgt;
		mc = widget.wmc.midiDisplay;

		this.view = PopUpMenu(parentView, rect)
		.enabled_(mc.model.value[index].learn != "X")
		.items_(["source..."] ++ CVWidget.midiSources.values.sort).maxWidth_(100);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |sel|
			var i = widget.midiConnectors.indexOf(this.connector);
			mc.model.value[i].src = CVWidget.midiSources.findKeyForValue(sel.item);
			mc.model.value[i].learn = "C";
			mc.model.value[i].toolTip = "Connect using selected parameters";
			mc.model.changedPerformKeys(widget.syncKeys, i);
		});
		this.prAddController;
	}

	// set the view to the specified connector's model value
	index_ { |connectorID|
		var display;

		connector = widget.midiConnectors[connectorID];
		mc.model.value[connectorID] !? {
			display = if (mc.model.value[connectorID].src == "source...") { 0 } {
				this.view.items.indexOfEqual(CVWidget.midiSources[mc.model.value[connectorID].src.asSymbol]);
			};
			this.view.value_(display)
		}
	}

	prAddController {
		var conID;
		mc.controller ?? {
			mc.controller = SimpleController(mc.model)
		};
		syncKey = \midiSrcSelect;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				conID = moreArgs[0];
				all[widget].do { |sel|
					if (sel.connector === widget.midiConnectors[conID]) {
						if (changer.value[conID].src.isNil or: { changer.value[conID].src == "source..." }) {
							defer {
								sel.view.value_(0);
								sel.view.enabled_(widget.wmc.midiConnections.model.value[conID].isNil);
							}
						} {
							defer {
								sel.view.value_(sel.items.indexOfEqual(CVWidget.midiSources[changer.value[conID].src.asSymbol]));
								sel.view.enabled_(widget.wmc.midiConnections.model.value[conID].isNil);
							}
						}
					}
				}
			})
		}
	}
}

MidiChanField : ConnectorElementView {
	classvar <all;
	var <connector, <widget;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, connectorID=0|
		^super.new.init(parent, widget, rect, connectorID);
	}

	init { |parentView, wdgt, rect, index|
		all[wdgt] ?? { all[wdgt] = List[] };
		all[wdgt].add(this);

		widget = wdgt;
		mc = widget.wmc.midiDisplay;
		this.view = TextField(parentView, rect)
		.enabled_(mc.model.value[index].learn != "X");
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |tf|
			var i = widget.midiConnectors.indexOf(this.connector);
			mc.model.value[i].chan = tf.string;
			mc.model.value[i].learn = "C";
			mc.model.value[i].toolTip = "Connect using selected parameters";
			mc.model.changedPerformKeys(widget.syncKeys, i);
		});
		this.prAddController;
	}

	// set the view to the specified connector's model value
	index_ { |connectorID|
		connector = widget.midiConnectors[connectorID];
		mc.model.value[connectorID] !? {
			this.view.string_(mc.model.value[connectorID].chan);
		}
	}

	prAddController {
		var conID;
		mc.controller ?? {
			mc.controller = SimpleController(mc.model)
		};
		syncKey = \midiChanField;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				conID = moreArgs[0];
				all[widget].do { |tf|
					if (tf.connector === widget.midiConnectors[conID]) {
						defer {
							tf.view.string_(changer.value[conID].chan);
							tf.view.enabled_(widget.wmc.midiConnections.model.value[conID].isNil);
						}
					}
				}
			})
		}
	}
}

MidiCtrlField : ConnectorElementView {
	classvar <all;
	var <connector, <widget;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, connectorID=0|
		^super.new.init(parent, widget, rect, connectorID);
	}

	init { |parentView, wdgt, rect, index|
		all[wdgt] ?? { all[wdgt] = List[] };
		all[wdgt].add(this);

		widget = wdgt;
		mc = widget.wmc.midiDisplay;
		this.view = TextField(parentView, rect)
		.enabled_(mc.model.value[index].learn != "X");
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |tf|
			var i = widget.midiConnectors.indexOf(this.connector);
			mc.model.value[i].ctrl = tf.string;
			mc.model.value[i].learn = "C";
			mc.model.value[i].toolTip = "Connect using selected parameters";
			mc.model.changedPerformKeys(widget.syncKeys, i);
		});
		this.prAddController;
	}

	// set the view to the specified connector's model value
	index_ { |connectorID|
		connector = widget.midiConnectors[connectorID];
		mc.model.value[connectorID] !? {
			this.view.string_(mc.model.value[connectorID].ctrl);
		}
	}

	prAddController {
		var conID;
		mc.controller ?? {
			mc.controller = SimpleController(mc.model)
		};
		syncKey = \midiCtrlField;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				conID = moreArgs[0];
				all[widget].do { |tf|
					if (tf.connector === widget.midiConnectors[conID]) {
						defer {
							tf.view.string_(changer.value[conID].ctrl);
							tf.view.enabled_(widget.wmc.midiConnections.model.value[conID].isNil);
						}
					}
				}
			})
		}
	}
}

MidiModeSelect : ConnectorElementView {
	classvar <all;
	var <connector, <widget;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, connectorID=0|
		^super.new.init(parent, widget, rect, connectorID);
	}

	init { |parentView, wdgt, rect, index|
		widget = wdgt;
		all[widget] ?? { all[wdgt] = List[] };
		all[widget].add(this);

		mc = widget.wmc.midiOptions;
		this.view = PopUpMenu(parentView, rect).items_(["0-127", "endless"]);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |sel|
			var i = widget.midiConnectors.indexOf(this.connector);
			// "My ID: %, my connector: %".format(MidiModeSelect.all[widget].indexOf(this), this.connector).postln;
			this.connector.setMidiMode(sel.value);
		});
		this.prAddController;
	}

	// index_ the view to the specified connector's model value
	index_ { |connectorID|
		connector = widget.midiConnectors[connectorID];
		mc.model.value[connectorID] !? {
			this.view.value_(mc.model.value[connectorID].midiMode)
		}
	}

	prAddController {
		var conID;
		mc.controller ?? {
			mc.controller = SimpleController(mc.model)
		};
		syncKey = \midiModeSelect;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				conID = moreArgs[0];
				all[widget].do { |sel|
					// [sel.connector, widget.midiConnectors[conID], this.connector].postln;
					if (sel.connector === widget.midiConnectors[conID]) {
						defer { sel.view.value_(changer[conID].value.midiMode) }
					}
				}
			})
		}
	}
}

MidiZeroNumberBox : ConnectorElementView {
	classvar <all;
	var <connector, <widget;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, connectorID=0|
		^super.new.init(parent, widget, rect, connectorID);
	}

	init { |parentView, wdgt, rect, index|
		all[wdgt] ?? { all[wdgt] = List[] };
		all[wdgt].add(this);

		widget = wdgt;
		mc = widget.wmc.midiOptions;
		this.view = NumberBox(parentView, rect);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |nb|
			// var i = widget.midiConnectors.indexOf(this.connector);
			this.connector.setMidiZero(nb.value);
		});
		this.prAddController;
	}

	// set the view to the specified connector's model value
	index_ { |connectorID|
		connector = widget.midiConnectors[connectorID];
		mc.model.value[connectorID] !? {
			this.view.value_(mc.model.value[connectorID].midiZero)
		}
	}

	prAddController {
		var conID;
		mc.controller ?? {
			mc.controller = SimpleController(mc.model)
		};
		syncKey = \midiZeroNumberBox;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				conID = moreArgs[0];
				all[widget].do { |nb|
					if (nb.connector === widget.midiConnectors[conID]) {
						defer { nb.view.value_(changer[conID].value.midiZero) }
					}
				}
			})
		}
	}
}

SnapDistanceNumberBox : ConnectorElementView {
	classvar <all;
	var <connector, <widget;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, connectorID=0|
		^super.new.init(parent, widget, rect, connectorID);
	}

	init { |parentView, wdgt, rect, index|
		all[wdgt] ?? { all[wdgt] = List[] };
		all[wdgt].add(this);

		widget = wdgt;
		mc = widget.wmc.midiOptions;
		this.view = NumberBox(parentView, rect).step_(0.1).clipLo_(0.0).clipHi_(1.0);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |nb|
			// var i = widget.midiConnectors.indexOf(this.connector);
			this.connector.setSnapDistance(nb.value);
		});
		this.prAddController;
	}

	// set the view to the specified connector's model value
	index_ { |connectorID|
		connector = widget.midiConnectors[connectorID];
		mc.model.value[connectorID] !? {
			this.view.value_(mc.model.value[connectorID].snapDistance)
		}
	}

	prAddController {
		var conID;
		mc.controller ?? {
			mc.controller = SimpleController(mc.model)
		};
		syncKey = \snapDistanceNumberBox;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				conID = moreArgs[0];
				all[widget].do { |nb|
					if (nb.connector === widget.midiConnectors[conID]) {
						defer { nb.view.value_(changer.value[conID].snapDistance) }
					}
				}
			})
		}
	}
}

MidiResolutionNumberBox : ConnectorElementView {
	classvar <all;
	var <connector, <widget;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, connectorID=0|
		^super.new.init(parent, widget, rect, connectorID);
	}

	init { |parentView, wdgt, rect, index|
		all[wdgt] ?? { all[wdgt] = List[] };
		all[wdgt].add(this);

		widget = wdgt;
		mc = widget.wmc.midiOptions;
		this.view = NumberBox(parentView, rect);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |nb|
			// var i = widget.midiConnectors.indexOf(this.connector);
			this.connector.setMidiResolution(nb.value);
		});
		this.prAddController;
	}

	// set the view to the specified connector's model value
	index_ { |connectorID|
		connector = widget.midiConnectors[connectorID];
		mc.model.value[connectorID] !? {
			this.view.value_(mc.model.value[connectorID].midiResolution)
		}
	}

	prAddController {
		var conID;
		mc.controller ?? {
			mc.controller = SimpleController(mc.model)
		};
		syncKey = \midiResolutionNumberBox;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				conID = moreArgs[0];
				all[widget].do { |nb|
					if (nb.connector === widget.midiConnectors[conID]) {
						defer { nb.view.value_(changer.value[conID].midiResolution) }
					}
				}
			})
		}
	}
}

SlidersPerGroupNumberTF : ConnectorElementView {
	classvar <all;
	var <connector, <widget;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, connectorID=0|
		^super.new.init(parent, widget, rect, connectorID);
	}

	init { |parentView, wdgt, rect, index|
		all[wdgt] ?? { all[wdgt] = List[] };
		all[wdgt].add(this);

		widget = wdgt;
		mc = widget.wmc.midiOptions;
		// TODO: make this a NumberBox starting at 0 (where 0 means nil)
		this.view = TextField(parentView, rect);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |tf|
			var ctrlb = if (tf.string.size.asBoolean) { tf.string };
			// preliminary fix!!
			this.connector.setCtrlButtonGroup(ctrlb.asInteger)
		});
		this.prAddController;
	}

	// set the view to the specified connector's model value
	index_ { |connectorID|
		connector = widget.midiConnectors[connectorID];
		mc.model.value[connectorID] !? {
			this.view.string_(mc.model.value[connectorID].ctrlButtonGroup)
		}
	}

	prAddController {
		var conID;
		mc.controller ?? {
			mc.controller = SimpleController(mc.model)
		};
		syncKey = \slidersPerGroupNumberTF;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				conID = moreArgs[0];
				all[widget].do { |tf|
					if (tf.connector === widget.midiConnectors[conID]) {
						defer { tf.view.string_(changer.value[conID].ctrlButtonGroup) }
					}
				}
			})
		}
	}
}

MidiInitButton : ConnectorElementView {
	classvar <all;
	var syncKey;


	*initClass {
		all = List[];
	}

	*new { |parent, rect|
		^super.new.init(parent, rect)
	}

	init { |parentView, rect|
		var midiConnectAll = {
			try { MIDIIn.connectAll } { |error|
				error.postln;
				"MIDIIn.connectAll failed. Please establish the necessary connections manually".warn;
			}
		};

		all.add(this);

		this.view = Button(parentView, rect)
		.action_({ |bt|
			if (MIDIClient.initialized) {
				MIDIClient.restart;
			} {
				MIDIClient.init;
			};
			try { MIDIIn.connectAll } { |error|
				error.postln;
				"MIDIIn.connectAll failed. Please establish the necessary connections manually.".warn;
			};
			MIDIClient.externalSources.do { |source|
				if (CVWidget.midiSources.values.includes(source.uid.asInteger).not, {
					// OSX/Linux specific tweek
					if(source.name == source.device) {
						CVWidget.midiSources.put(source.uid.asSymbol, "% (%)".format(source.name, source.uid))
					} {
						CVWidget.midiSources.put(source.uid.asSymbol, "% (%)".format(source.name, source.uid))
					}
				})
			};
			CVWidget.midiInitialized.model.value_(MIDIClient.initialized).changedPerformKeys(CVWidget.syncKeys);
		});
		this.view.onClose_({ this.close });


		if (MIDIClient.initialized) {
			this.view.states_([["reinit MIDI", Color.white, Color.red]]);
		} {
			this.view.states_([["init MIDI", Color.black, Color.green]]);
		};
		this.prAddController;
	}

	index_ {}

	prAddController {
		CVWidget.midiInitialized.controller ?? {
			CVWidget.midiInitialized.controller = SimpleController(CVWidget.midiInitialized.model)
		};
		syncKey = \midiInitButton;
		CVWidget.syncKeys.indexOf(syncKey) ?? {
			CVWidget.prAddSyncKey(syncKey, true);
			CVWidget.midiInitialized.controller.put(syncKey, { |changer, what|
				all.do { |bt|
					if (changer.value) {
						bt.view.states_([["reinit MIDI", Color.white, Color.red]]);
					} {
						bt.view.states_([["init MIDI", Color.black, Color.green]]);
					};
				};
				// in case MIDI hasn't been initialized on startup
				MidiSrcSelect.all.do { |list|
					list.do { |sel|
						sel.view.items_(["source..."] ++ CVWidget.midiSources.values.sort);
					}
				}
			})
		}
	}

	close {
		this.remove;
		this.viewDidClose;
		all.remove(this);
		if (all.isEmpty) {
			CVWidget.midiInitialized.controller.removeAt(syncKey);
			CVWidget.prRemoveSyncKey(syncKey, true);
		}
	}

}

MidiConnectorRemoveButton : ConnectorElementView {
	classvar <all;
	var <connector, <widget;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, connectorID=0|
		^super.new.init(parent, widget, rect, connectorID);
	}

	init { |parentView, wdgt, rect, index|
		all[wdgt] ?? { all[wdgt] = List[] };
		all[wdgt].add(this);

		widget = wdgt;
		this.index_(index);
		this.view = Button(parentView, rect)
		.states_([["remove Connector", Color.white, Color(0, 0.5, 0.5)]])
		.action_({ this.connector.remove })
	}

	index_ { |connectorID|
		connector = widget.midiConnectors[connectorID];
	}

	close {
		this.remove;
		this.viewDidClose;
		all[widget].remove(this);
	}

}
