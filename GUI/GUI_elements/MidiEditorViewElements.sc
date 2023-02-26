// MIDI editors

MidiConnectorElementView : SCViewHolder {
	var widget, mc, syncKey;

	close {
		this.remove;
		this.viewDidClose;
		this.class.all[widget].remove(this);
		this.class.all.detect(_.notEmpty) ?? {
			mc.controller.removeAt(syncKey);
			widget.prRemoveSyncKey(syncKey, true);
		}
	}

}

MidiConnectorNameField : MidiConnectorElementView {
	classvar <all;
	var <connector;

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
				var conID = widget.midiConnectors.indexOf(connector);
				all[widget].do { |tf|
					if (tf.connector === connector) {
						tf.view.string_(changer.value[conID]);
					}
				};
			})
		}
	}
}

MidiConnectorSelect : MidiConnectorElementView {
	classvar <all;
	var <connector;

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
		mc.controller ?? {
			mc.controller = SimpleController(mc.model)
		};
		syncKey = \midiConnectorSelect;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				var items, conID = widget.midiConnectors.indexOf(connector);
				all[widget].do { |sel|
					items = sel.view.items;
					items[conID] = connector.name;
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
// the widget's o0scConnectors / midiConnectors lists. Hence, rather determine
// the current index from querying the widget's oscConnectors / midiConnectors list.

MidiLearnButton : MidiConnectorElementView {
	classvar <all;
	var <connector;

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
		]);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |bt|
			var i = widget.midiConnectors.indexOf(connector);
			mc.model[i].value_((
				learn: bt.states[bt.value][0],
				src: mc.model[i].value.src,
				chan: mc.model[i].value.chan,
				ctrl: mc.model[i].value.ctrl
			));
			mc.model.value.changedKeys(widget.syncKeys);
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
		mc.controller ?? {
			mc.controller = SimpleController(mc.model)
		};
		syncKey = \midiLearnButton;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true);
			// the following is global for all MidiLearnButtons
			// there must be no notion of 'this'
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				var pos, conID = widget.midiConnectors.indexOf(connector);
				all[widget].do { |but|
					if (but.connector === connector) {
						pos = but.view.states.detectIndex { |a, i|
							a[0] == changer[conID].value.learn
						};
						but.view.value_(pos);
					}
				}
			})
		}
	}
}

MidiSrcSelect : MidiConnectorElementView {
	classvar <all;
	var <connector;
	// preliminary
	classvar <>midiSources;

	*initClass {
		all = ();
		// for testing - remove
		MidiSrcSelect.midiSources_([12345, 15243]);
	}

	*new { |parent, widget, rect, connectorID=0|
		^super.new.init(parent, widget, rect, connectorID);
	}

	init { |parentView, wdgt, rect, index|
		all[wdgt] ?? { all[wdgt] = List[] };
		all[wdgt].add(this);

		widget = wdgt;
		mc = widget.wmc.midiDisplay;
		this.view = PopUpMenu(parentView, rect).items_(["source..."] ++ MidiSrcSelect.midiSources);
		this.index_(index);
		this.view.action_({ |sel|
			var i = widget.midiConnectors.indexOf(connector);
			mc.model[i].value_((
				learn: mc.model[i].value.learn,
				src: sel.item,
				chan: mc.model[i].value.chan,
				ctrl: mc.model[i].value.ctrl
			));
			mc.model.value.changedKeys(widget.syncKeys);
		});
		// TODO: add dependency to MIDI inititialisation -> fill items with list of sources
		this.prAddController;
	}

	// set the view to the specified connector's model value
	index_ { |connectorID|
		connector = widget.midiConnectors[connectorID];
		this.view.value_(
			this.view.items.indexOfEqual(mc.model[connectorID].value.src);
		)
	}

	prAddController {
		mc.controller ?? {
			mc.controller = SimpleController(mc.model)
		};
		syncKey = \midiSrcSelect;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				var conID = widget.midiConnectors.indexOf(connector);
				all[widget].do { |sel|
					if (sel.connector === connector) {
						sel.view.value_(sel.items.indexOfEqual(changer[conID].value.src))
					}
				}
			})
		}
	}
}

MidiChanField : MidiConnectorElementView {
	classvar <all;
	var <connector;

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
				learn: mc.model[i].value.learn,
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
		mc.controller ?? {
			mc.controller = SimpleController(mc.model)
		};
		syncKey = \midiChanField;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				var conID = widget.midiConnectors.indexOf(connector);
				all[widget].do { |tf|
					if (tf.connector === connector) {
						tf.view.string_(changer[conID].value.chan)
					}
				}
			})
		}
	}
}

MidiCtrlField : MidiConnectorElementView {
	classvar <all;
	var <connector;

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
				learn: mc.model[i].value.learn,
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
		mc.controller ?? {
			mc.controller = SimpleController(mc.model)
		};
		syncKey = \midiCtrlField;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				var conID = widget.midiConnectors.indexOf(connector);
				all[widget].do { |tf|
					if (tf.connector === connector) {
						tf.view.string_(changer[conID].value.ctrl)
					}
				}
			})
		}
	}
}

MidiModeSelect : MidiConnectorElementView {
	classvar <all;
	var <connector;

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
				midiMean: mc.model[i].value.midiMean,
				ctrlButtonBank: mc.model[i].value.ctrlButtonBank,
				midiResolution: mc.model[i].value.midiResolution,
				softWithin: mc.model[i].value.softWithin
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
		mc.controller ?? {
			mc.controller = SimpleController(mc.model)
		};
		syncKey = \midiModeSelect;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				var conID = widget.midiConnectors.indexOf(connector);
				all[widget].do { |sel|
					if (sel.connector === connector) {
						sel.view.value_(changer[conID].value.midiMode)
					}
				}
			})
		}
	}
}

MidiMeanNumberBox : MidiConnectorElementView {
	classvar <all;
	var <connector;

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
				midiMean: nb.value,
				ctrlButtonBank: mc.model[i].value.ctrlButtonBank,
				midiResolution: mc.model[i].value.midiResolution,
				softWithin: mc.model[i].value.softWithin
			));
			mc.model.value.changedKeys(widget.syncKeys);
		});
		this.prAddController;
	}

	// set the view to the specified connector's model value
	index_ { |connectorID|
		connector = widget.midiConnectors[connectorID];
		this.view.value_(mc.model[connectorID].value.midiMean);
	}

	prAddController {
		mc.controller ?? {
			mc.controller = SimpleController(mc.model)
		};
		syncKey = \midiMeanNumberBox;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				var conID = widget.midiConnectors.indexOf(connector);
				all[widget].do { |nb|
					if (nb.connector === connector) {
						nb.view.value_(changer[conID].value.midiMean)
					}
				}
			})
		}
	}
}

SoftWithinNumberBox : MidiConnectorElementView {
	classvar <all;
	var <connector;

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
				midiMean: mc.model[i].value.midiMean,
				ctrlButtonBank: mc.model[i].value.ctrlButtonBank,
				midiResolution: mc.model[i].value.midiResolution,
				softWithin: nb.value
			));
			mc.model.value.changedKeys(widget.syncKeys);
		});
		this.prAddController;
	}

	// set the view to the specified connector's model value
	index_ { |connectorID|
		connector = widget.midiConnectors[connectorID];
		this.view.value_(mc.model[connectorID].value.softWithin)
	}

	prAddController {
		mc.controller ?? {
			mc.controller = SimpleController(mc.model)
		};
		syncKey = \softWithinNumberBox;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				var conID = widget.midiConnectors.indexOf(connector);
				all[widget].do { |nb|
					if (nb.connector === connector) {
						nb.view.value_(changer[conID].value.softWithin)
					}
				}
			})
		}
	}
}

MidiResolutionNumberBox : MidiConnectorElementView {
	classvar <all;
	var <connector;

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
				midiMean: mc.model[i].value.midiMean,
				ctrlButtonBank: mc.model[i].value.ctrlButtonBank,
				midiResolution: nb.value,
				softWithin: mc.model[i].value.softWithin
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
		mc.controller ?? {
			mc.controller = SimpleController(mc.model)
		};
		syncKey = \midiResolutionNumberBox;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				var conID = widget.midiConnectors.indexOf(connector);
				all[widget].do { |nb|
					if (nb.connector === connector) {
						nb.view.value_(changer[conID].value.midiResolution)
					}
				}
			})
		}
	}
}

SlidersPerBankNumberTF : MidiConnectorElementView {
	classvar <all;
	var <connector;

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
				midiMean: mc.model[i].value.midiMean,
				ctrlButtonBank: ctrlb,
				midiResolution: mc.model[i].value.midiResolution,
				softWithin: mc.model[i].value.softWithin
			));
			mc.model.value.changedKeys(widget.syncKeys);
		});
		this.prAddController;
	}

	// set the view to the specified connector's model value
	index_ { |connectorID|
		connector = widget.midiConnectors[connectorID];
		this.view.string_(mc.model[connectorID].value.ctrlButtonBank)
	}

	prAddController {
		mc.controller ?? {
			mc.controller = SimpleController(mc.model)
		};
		syncKey = \slidersPerBankNumberBox;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				var conID = widget.midiConnectors.indexOf(connector);
				all[widget].do { |tf|
					if (tf.connector === connector) {
						tf.view.string_(changer[conID].value.ctrlButtonBank)
					}
				}
			})
		}
	}

}
