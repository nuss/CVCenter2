// MIDI editors

MidiConnectorNameField : SCViewHolder {

}

MidiConnectorSelect : SCViewHolder {
	classvar <all;
	var widget, mc;

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
		this.view = PopUpMenu(parentView)
		.items_(widget.midiConnectors.collect(_.name) ++ ["add MidiConnector..."]);
		this.view.value_(index);
		this.view.onClose_({ this.close });
	}

	index_ { |connectorID|
		this.view.value_(connectorID);
	}

	close {
		this.remove;
		this.viewDidClose;
		all[widget].remove(this);
	}
}

// Elements must not hold a fixed ID as connectors can get deleted from
// the widget's o0scConnectors / midiConnectors lists. Hence, rather determine
// the current index from querying the widget's oscConnectors / midiConnectors list.

MidiLearnButton : SCViewHolder {
	classvar <all, c = 0;
	var widget, mc;
	var <connector, syncKey;

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

	close {
		this.remove;
		this.viewDidClose;
		all[widget].remove(this);
		mc.controller.removeAt(syncKey);
		widget.prRemoveSyncKey(syncKey, true);
	}

	prAddController {
		mc.controller !? {
			syncKey = (\midiLearnButton_ ++ c).asSymbol;
			c = c + 1;
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				var pos, conID = widget.midiConnectors.indexOf(connector);
				all[widget].do { |but|
					pos = but.view.states.detectIndex { |a, i|
						a[0] == changer[conID].value.learn
					};
					this.view.value_(pos);
				}
			})
		}
	}
}

MidiSrcSelect : SCViewHolder {
	classvar <all, c = 0;
	// preliminary
	classvar <>midiSources;
	var widget, mc;
	var <connector, syncKey;

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
		this.view = PopUpMenu(parentView, rect).items_(["source..."] ++ MidiSrcSelect.midiSources);
		this.index_(index);
		this.view.action_({ |sel|
			var i = widget.midiConnectors.indexOf(connector);
			mc.model[i].value_((
				learn: mc.model[i].value.learn,
				src: sel.items[sel.value],
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
		var val;

		connector = widget.midiConnectors[connectorID];
		// FIXME
		this.view.value_(
			// "this.view.items: %, classes: %".format(this.view.items, this.view.items.collect(_.class)).postln;
			// "mc.model[%].value.src: %".format(connectorID, mc.model[connectorID].value.src).postln;
			this.view.items.indexOfEqual(mc.model[connectorID].value.src);
		)
	}

	close {
		this.remove;
		this.viewDidClose;
		all[widget].remove(this);
		mc.controller.removeAt(syncKey);
		widget.prRemoveSyncKey(syncKey, true);
	}

	prAddController {
		mc.controller !? {
			syncKey = (\midiSrcSelect_ ++ c).asSymbol;
			c = c + 1;
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				var conID = widget.midiConnectors.indexOf(connector);
				all[widget].do { |sel|
					sel.view.value_(changer[conID].value.src)
				}
			})
		}
	}
}

MidiChanField : SCViewHolder {
	classvar <all, c = 0;
	var widget, mc;
	var <connector, syncKey;

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

	close {
		this.remove;
		this.viewDidClose;
		all[widget].remove(this);
		mc.controller.removeAt(syncKey);
		widget.prRemoveSyncKey(syncKey, true);
	}

	prAddController {
		mc.controller !? {
			syncKey = (\midiChanField_ ++ c).asSymbol;
			c = c + 1;
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				var conID = widget.midiConnectors.indexOf(connector);
				all[widget].do { |tf|
					tf.view.string_(changer[conID].value.chan)
				}
			})
		}
	}
}

MidiCtrlField : SCViewHolder {
	classvar <all, c = 0;
	var widget, mc;
	var <connector, syncKey;

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

	close {
		this.remove;
		this.viewDidClose;
		all[widget].remove(this);
		mc.controller.removeAt(syncKey);
		widget.prRemoveSyncKey(syncKey, true);
	}

	prAddController {
		mc.controller !? {
			syncKey = (\midiCtrlField_ ++ c).asSymbol;
			c = c + 1;
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				var conID = widget.midiConnectors.indexOf(connector);
				all[widget].do { |tf|
					tf.view.string_(changer[conID].value.ctrl)
				}
			})
		}
	}
}

MidiModeSelect : SCViewHolder {
	classvar <all, c = 0;
	var widget, mc;
	var <connector, syncKey;

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
			mc.model[index].value_((
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

	close {
		this.remove;
		this.viewDidClose;
		all[widget].remove(this);
		mc.controller.removeAt(syncKey);
		widget.prRemoveSyncKey(syncKey, true);
	}

	prAddController {
		mc.controller !? {
			syncKey = (\midiModeSelect_ ++ c).asSymbol;
			c = c + 1;
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				var conID = widget.midiConnectors.indexOf(connector);
				"changer[%].value.midiMode: %".format(conID, changer[conID].value.midiMode).postln;
				all[widget].do { |sel|
					sel.view.value_(changer[conID].value.midiMode)
				}
			})
		}
	}
}

MidiMeanNumberBox : SCViewHolder {
	classvar <all, c = 0;
	var widget, mc;
	var <connector, syncKey;

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

	close {
		this.remove;
		this.viewDidClose;
		all[widget].remove(this);
		mc.controller.removeAt(syncKey);
		widget.prRemoveSyncKey(syncKey, true);
	}

	prAddController {
		mc.controller !? {
			syncKey = (\midiMeanNumberBox_ ++ c).asSymbol;
			c = c + 1;
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				var conID = widget.midiConnectors.indexOf(connector);
				all[widget].do { |nb|
					nb.view.value_(changer[conID].value.midiMean)
				}
			})
		}
	}
}

SoftWithinNumberBox : SCViewHolder {
	classvar <all, c = 0;
	var widget, mc;
	var <connector, syncKey;

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
			mc.model[index].value_((
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

	close {
		this.remove;
		this.viewDidClose;
		all[widget].remove(this);
		mc.controller.removeAt(syncKey);
		widget.prRemoveSyncKey(syncKey, true);
	}

	prAddController {
		mc.controller !? {
			syncKey = (\softWithinNumberBox_ ++ c).asSymbol;
			c = c + 1;
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				var conID = widget.midiConnectors.indexOf(connector);
				all[widget].do { |nb|
					nb.view.value_(changer[conID].value.softWithin)
				}
			})
		}
	}
}

MidiResolutionNumberBox : SCViewHolder {
	classvar <all, c = 0;
	var widget, mc;
	var <connector, syncKey;

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

	close {
		this.remove;
		this.viewDidClose;
		all[widget].remove(this);
		mc.controller.removeAt(syncKey);
		widget.prRemoveSyncKey(syncKey, true);
	}

	prAddController {
		mc.controller !? {
			syncKey = (\midiResolutionNumberBox_ ++ c).asSymbol;
			c = c + 1;
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				var conID = widget.midiConnectors.indexOf(connector);
				all[widget].do { |nb|
					nb.view.value_(changer[conID].value.midiResolution)
				}
			})
		}
	}
}

SlidersPerBankNumberTF : SCViewHolder {
	classvar <all, c = 0;
	var widget, mc;
	var <connector, syncKey;

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

	close {
		this.remove;
		this.viewDidClose;
		all[widget].remove(this);
		mc.controller.removeAt(syncKey);
		widget.prRemoveSyncKey(syncKey, true);
	}

	prAddController {
		mc.controller !? {
			syncKey = (\slidersPerBankNumberBox_ ++ c).asSymbol;
			c = c + 1;
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				var conID = widget.midiConnectors.indexOf(connector);
				all[widget].do { |tf|
					tf.view.string_(changer[conID].value.ctrlButtonBank)
				}
			})
		}
	}
}

// OSC Editors

OscConnectorSelect : SCViewHolder {
	classvar <all;

	*initClass {
		all = ();
	}
}
