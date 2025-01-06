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
			connector.name_(tf.string.asSymbol)
		});
		this.view.onClose_({ this.close });
		this.prAddController;
	}

	index_ { |connectorID|
		connector = widget.midiConnectors[connectorID];
		this.view.string_(mc.model.value[connectorID]);
	}

	prAddController {
		mc.controller ?? {
			mc.controller = SimpleController(mc.model)
		};
		syncKey = \midiConnectorName;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				var conID = moreArgs[0];
				all[widget].do { |tf|
					if (tf.connector === connector) {
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
					sel.view.items_(items);
					if (sel.connector === connector) {
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
		widget = wdgt;
		all[widget] ?? { all.put(widget, List[]) };
		all[widget].add(this);

		mc = widget.wmc.midiDisplay;
		this.view = Button(parentView, rect).states_([
			["L", Color.white, Color.blue],
			["X", Color.white, Color.red]
		]).maxWidth_(25);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |bt|
			var i = widget.midiConnectors.indexOf(connector);
			var src, chan, ctrl;
			mc.model[i].value_((
				learn: bt.states[bt.value][0],
				src: mc.model[i].value.src,
				chan: mc.model[i].value.chan,
				ctrl: mc.model[i].value.ctrl
			));
			mc.model.value.changedKeys(widget.syncKeys);
			if (mc.model[i].value.learn == "X") {
				if (mc.model[i].value.src != "source...") { src = mc.model[i].value.src };
				if (mc.model[i].value.chan != "chan") { chan = mc.model[i].value.chan };
				if (mc.model[i].value.ctrl != "ctrl") { ctrl = mc.model[i].value.ctrl };
				widget.midiConnect(connector, src, chan, ctrl);
				if (src.notNil or: { chan.notNil or: { ctrl.notNil }}) {
					all[widget].do { |b|
						b.states_([
							["L", Color.white, Color.blue],
							["X", Color.white, Color.red]
						]).value_(1)
					}
				}
			}
			{
				widget.midiDisconnect(connector)
			}
		});
		this.prAddController;
	}

	// the connector's ID will be dynamic and change
	// any time a connector with a smaller ID in the widget's
	// midiConnectors list gets deleted!!!
	index_ { |connectorID|
		// we need the connector, not its current ID in widget.midiConnectors
		connector = widget.midiConnectors[connectorID];
		mc.model[connectorID].value.learn.switch(
			"X", {
				this.view.value_(1)
			},
			"L", {
				this.view.value_(0)
			}
		)
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
			// there must be no notion of 'this' as all MdiLearnButton instances are affected
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				conID = widget.midiConnectors.indexOf(connector);
				all[widget].do { |but|
					if (changer[conID].value.learn == "C") {
						but.states_([
							["C", Color.black, Color.green],
							["X", Color.white, Color.red]
						])
					};
					if (but.connector === connector) {
						pos = but.view.states.detectIndex { |a, i|
							a[0] == changer[conID].value.learn
						};
						defer { but.view.value_(pos) }
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

		this.view = PopUpMenu(
			parentView, rect).items_(["source..."] ++ CVWidget.midiSources.values.sort
		).maxWidth_(100);
		this.index_(index);
		this.view.action_({ |sel|
			var i = widget.midiConnectors.indexOf(connector);
			mc.model[i].value_((
				learn: "C",
				src: CVWidget.midiSources.findKeyForValue(sel.item),
				chan: mc.model[i].value.chan,
				ctrl: mc.model[i].value.ctrl
			));
			mc.model.value.changedKeys(widget.syncKeys);
		});
		this.prAddController;
	}

	// set the view to the specified connector's model value
	index_ { |connectorID|
		var display;

		connector = widget.midiConnectors[connectorID];
		display = if (mc.model[connectorID].value.src == "source...") { 0 } {
			this.view.items.indexOfEqual(CVWidget.midiSources[mc.model[connectorID].value.src.asSymbol]);
		};
		this.view.value_(display)
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
				conID = widget.midiConnectors.indexOf(connector);
				all[widget].do { |sel|
					if (sel.connector === connector) {
						if (changer[conID].value.src.isNil or: { changer[conID].value.src == "source..." }) {
							defer { sel.view.value_(0) }
						} {
							defer {
								sel.view.value_(sel.items.indexOfEqual(CVWidget.midiSources[changer[conID].value.src.asSymbol]));
							}
						}
					};
					defer { sel.view.enabled_(widget.wmc.midiConnections.model[conID].value.isNil) }
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
		this.view = TextField(parentView, rect);
		this.index_(index);
		this.view.action_({ |tf|
			var i = widget.midiConnectors.indexOf(connector);
			mc.model[i].value_((
				learn: "C",
				src: mc.model[i].value.src,
				chan: tf.string,
				ctrl: mc.model[i].value.ctrl
			));
			mc.model.value.changedKeys(widget.syncKeys);
		});
		this.prAddController;
	}

	// set the view to the specified connector's model value
	index_ { |connectorID|
		connector = widget.midiConnectors[connectorID];
		this.view.string_(mc.model[connectorID].value.chan);
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
				conID = widget.midiConnectors.indexOf(connector);
				all[widget].do { |tf|
					if (tf.connector === connector) {
						defer { tf.view.string_(changer[conID].value.chan) }
					};
					defer { tf.view.enabled_(widget.wmc.midiConnections.model[conID].value.isNil) }
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
		this.view = TextField(parentView, rect);
		this.index_(index);
		this.view.action_({ |tf|
			var i = widget.midiConnectors.indexOf(connector);
			mc.model[i].value_((
				learn: "C",
				src: mc.model[i].value.src,
				chan: mc.model[i].value.chan,
				ctrl: tf.string
			));
			mc.model.value.changedKeys(widget.syncKeys);
		});
		this.prAddController;
	}

	// set the view to the specified connector's model value
	index_ { |connectorID|
		connector = widget.midiConnectors[connectorID];
		this.view.string_(mc.model[connectorID].value.ctrl);
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
				conID = widget.midiConnectors.indexOf(connector);
				all[widget].do { |tf|
					if (tf.connector === connector) {
						defer { tf.view.string_(changer[conID].value.ctrl) }
					};
					defer { tf.view.enabled_(widget.wmc.midiConnections.model[conID].value.isNil) }
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
		this.view = PopUpMenu(parentView, rect).items_(["0-127", "+/-"]);
		this.index_(index);
		this.view.action_({ |sel|
			var i = widget.midiConnectors.indexOf(connector);
			mc.model[i].value_((
				midiMode: sel.value,
				midiZero: mc.model[i].value.midiZero,
				ctrlButtonGroup: mc.model[i].value.ctrlButtonGroup,
				midiResolution: mc.model[i].value.midiResolution,
				snapDistance: mc.model[i].value.snapDistance
			));
			mc.model.value.changedKeys(widget.syncKeys);
		});
		this.prAddController;
	}

	// index_ the view to the specified connector's model value
	index_ { |connectorID|
		connector = widget.midiConnectors[connectorID];
		this.view.value_(mc.model[connectorID].value.midiMode)
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
				conID = widget.midiConnectors.indexOf(connector);
				all[widget].do { |sel|
					if (sel.connector === connector) {
						sel.view.value_(changer[conID].value.midiMode)
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
		this.index_(index);
		this.view.action_({ |nb|
			var i = widget.midiConnectors.indexOf(connector);
			mc.model[i].value_((
				midiMode: mc.model[i].value.midiMode,
				midiZero: nb.value,
				ctrlButtonGroup: mc.model[i].value.ctrlButtonGroup,
				midiResolution: mc.model[i].value.midiResolution,
				snapDistance: mc.model[i].value.snapDistance
			));
			mc.model.value.changedKeys(widget.syncKeys);
		});
		this.prAddController;
	}

	// set the view to the specified connector's model value
	index_ { |connectorID|
		connector = widget.midiConnectors[connectorID];
		this.view.value_(mc.model[connectorID].value.midiZero);
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
				conID = widget.midiConnectors.indexOf(connector);
				all[widget].do { |nb|
					if (nb.connector === connector) {
						nb.view.value_(changer[conID].value.midiZero)
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
		this.index_(index);
		this.view.action_({ |nb|
			var i = widget.midiConnectors.indexOf(connector);
			mc.model[i].value_((
				midiMode: mc.model[i].value.midiMode,
				midiZero: mc.model[i].value.midiZero,
				ctrlButtonGroup: mc.model[i].value.ctrlButtonGroup,
				midiResolution: mc.model[i].value.midiResolution,
				snapDistance: nb.value
			));
			mc.model.value.changedKeys(widget.syncKeys);
		});
		this.prAddController;
	}

	// set the view to the specified connector's model value
	index_ { |connectorID|
		connector = widget.midiConnectors[connectorID];
		this.view.value_(mc.model[connectorID].value.snapDistance)
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
				conID = widget.midiConnectors.indexOf(connector);
				all[widget].do { |nb|
					if (nb.connector === connector) {
						nb.view.value_(changer[conID].value.snapDistance)
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
		this.index_(index);
		this.view.action_({ |nb|
			var i = widget.midiConnectors.indexOf(connector);
			mc.model[i].value_((
				midiMode: mc.model[i].value.midiMode,
				midiZero: mc.model[i].value.midiZero,
				ctrlButtonGroup: mc.model[i].value.ctrlButtonGroup,
				midiResolution: nb.value,
				snapDistance: mc.model[i].value.snapDistance
			));
			mc.model.value.changedKeys(widget.syncKeys);
		});
		this.prAddController;
	}

	// set the view to the specified connector's model value
	index_ { |connectorID|
		connector = widget.midiConnectors[connectorID];
		this.view.value_(mc.model[connectorID].value.midiResolution)
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
				conID = widget.midiConnectors.indexOf(connector);
				all[widget].do { |nb|
					if (nb.connector === connector) {
						nb.view.value_(changer[conID].value.midiResolution)
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
		this.view = TextField(parentView, rect);
		this.index_(index);
		this.view.action_({ |tf|
			var ctrlb = if (tf.string.size.asBoolean) { tf.string };
			var i = widget.midiConnectors.indexOf(connector);
			mc.model[i].value_((
				midiMode: mc.model[i].value.midiMode,
				midiZero: mc.model[i].value.midiZero,
				ctrlButtonGroup: ctrlb,
				midiResolution: mc.model[i].value.midiResolution,
				snapDistance: mc.model[i].value.snapDistance
			));
			mc.model.value.changedKeys(widget.syncKeys);
		});
		this.prAddController;
	}

	// set the view to the specified connector's model value
	index_ { |connectorID|
		connector = widget.midiConnectors[connectorID];
		this.view.string_(mc.model[connectorID].value.ctrlButtonGroup)
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
				conID = widget.midiConnectors.indexOf(connector);
				all[widget].do { |tf|
					if (tf.connector === connector) {
						tf.view.string_(changer[conID].value.ctrlButtonGroup)
					}
				}
			})
		}
	}
}

