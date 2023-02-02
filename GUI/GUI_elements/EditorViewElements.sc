ConnectionSelect : SCViewHolder {
	classvar all;
	var widget, mc;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect|
		^super.new.init(parent, widget, rect);
	}

	init { |parentView, wdgt, rect|
		all[wdgt] ?? { all[wdgt] = List[] };
		all[wdgt].add(this);

		widget = wdgt;
		mc = widget.wmc.midiDisplay;
		rect ?? { rect = Point(100, 20) };
		this.view = PopUpMenu(parentView)
		.items_(["Select connection..."] ++ widget.midiConnectors)
	}
}

// Elements must not hold a fixed ID as connectors can get deleted from
// the widget's o0scConnectors / midiConnectors lists. Hence, rather determine
// the current index from querying the widget's oscConnectors / midiConnectors list.

MidiLearnButton : SCViewHolder {
	classvar all, c = 0;
	var widget, mc;
	var connector, syncKey;

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
		this.set(index);
		this.prAddController;
	}

	// the connector's ID will be dynamic and change
	// any time a connector with a smaller ID in the widget's
	// midiConnectors list gets deleted!!!
	set { |connectorID|
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
		this.viewDidClose;
		all[widget].remove(this);
		if (all[widget].isEmpty) {
			mc.controller.removeAt(syncKey);
			widget.prRemoveSyncKey(syncKey, true);
		}
	}

	prAddController {
		mc.controller !? {
			syncKey = (\midiLearnButton_ ++ c).asSymbol;
			c = c + 1;
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				var pos, conID = widget.midiConnectors.indexOf(connector);
				[moreArgs, conID].postln;
				if (moreArgs[0] == conID) {
					all[widget].do { |but|
						pos = but.view.states.detectIndex { |a, i|
							a[0] == changer[conID].value.learn
						};
						this.view.value_(pos);
					}
				}
			})
		}
	}}

MidiSrcSelect : SCViewHolder {
	classvar all, c = 0;
	var widget, mc;
	var connector, syncKey;

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
		this.view = PopUpMenu(parentView, rect).items_(["source..."]);
		this.set(index);
		// TODO: add dependency to MIDI inititialisation -> fill items with list of sources
		this.prAddController;
	}

	// set the view to the specified connector's model value
	set { |connectorID|
		connector = widget.midiConnectors[connectorID];
		this.view.value_(
			this.view.items.detectIndex { |it, i| it == mc.model[connectorID].value.src }
		);
	}

	close {
		this.viewDidClose;
		all[widget].remove(this);
		if (all[widget].isEmpty) {
			mc.controller.removeAt(syncKey);
			widget.prRemoveSyncKey(syncKey, true);
		}
	}

	prAddController {
		mc.controller !? {
			syncKey = (\midiSrcSelect_ ++ c).asSymbol;
			c = c + 1;
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				var conID = widget.midiConnectors.indexOf(connector);
				if (moreArgs[0] == conID) {
					all[widget].do { |sel|
						sel.view.value_(changer[conID].value.src)
					}
				}
			})
		}
	}
}

MidiChanField : SCViewHolder {
	classvar all, c = 0;
	var widget, mc;
	var connector, syncKey;

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
		this.set(index);
		this.prAddController;
	}

	// set the view to the specified connector's model value
	set { |connectorID|
		connector = widget.midiConnectors[connectorID];
		this.view.string_(mc.model[connectorID].value.chan);
	}

	close {
		this.viewDidClose;
		all[widget].remove(this);
		if (all[widget].isEmpty) {
			mc.controller.removeAt(syncKey);
			widget.prRemoveSyncKey(syncKey, true);
		}
	}

	prAddController {
		mc.controller !? {
			syncKey = (\midiChanField_ ++ c).asSymbol;
			c = c + 1;
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				var conID = widget.midiConnectors.indexOf(connector);
				if (moreArgs[0] == conID) {
					all[widget].do { |tf|
						tf.view.string_(changer[conID].value.chan)
					}
				}
			})
		}
	}
}

MidiCtrlField : SCViewHolder {
	classvar all, c = 0;
	var widget, mc;
	var connector, syncKey;

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
		this.set(index);
		this.prAddController;
	}

	// set the view to the specified connector's model value
	set { |connectorID|
		connector = widget.midiConnectors[connectorID];
		this.view.string_(mc.model[connectorID].value.ctrl);
	}

	close {
		this.viewDidClose;
		all[widget].remove(this);
		if (all[widget].isEmpty) {
			mc.controller.removeAt(syncKey);
			widget.prRemoveSyncKey(syncKey, true);
		}
	}

	prAddController {
		mc.controller !? {
			syncKey = (\midiCtrlField_ ++ c).asSymbol;
			c = c + 1;
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				var conID = widget.midiConnectors.indexOf(connector);
				if (moreArgs[0] == conID) {
					all[widget].do { |tf|
						tf.view.string_(changer[conID].value.ctrl)
					}
				}
			})
		}
	}
}

MidiModeSelect : SCViewHolder {
	classvar all, c = 0;
	var widget, mc;
	var connector, syncKey;

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
		this.view = PopUpMenu(parentView, rect).items_(["0-127", "+/-"]);
		this.set(index);
		this.prAddController;
	}

	// set the view to the specified connector's model value
	set { |connectorID|
		connector = widget.midiConnectors[connectorID];
		this.view.value_(mc.model[connectorID].value.midiMode)
	}

	close {
		this.viewDidClose;
		all[widget].remove(this);
		if (all[widget].isEmpty) {
			mc.controller.removeAt(syncKey);
			widget.prRemoveSyncKey(syncKey, true);
		}
	}

	prAddController {
		mc.controller !? {
			syncKey = (\midiModeSelect_ ++ c).asSymbol;
			c = c + 1;
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				var conID = widget.midiConnectors.indexOf(connector);
				if (moreArgs[0] == conID) {
					all[widget].do { |sel|
						sel.view.value_(changer[conID].value.midiMode)
					}
				}
			})
		}
	}
}

MidiMeanNumberBox : SCViewHolder {
	classvar all, c = 0;
	var widget, mc;
	var connector, syncKey;

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
		this.set(index);
		this.prAddController;
	}

	// set the view to the specified connector's model value
	set { |connectorID|
		connector = widget.midiConnectors[connectorID];
		this.view.value_(mc.model[connectorID].value.midiMean);
	}

	close {
		this.viewDidClose;
		all[widget].remove(this);
		if (all[widget].isEmpty) {
			mc.controller.removeAt(syncKey);
			widget.prRemoveSyncKey(syncKey, true);
		}
	}

	prAddController {
		mc.controller !? {
			syncKey = (\midiMeanNumberBox_ ++ c).asSymbol;
			c = c + 1;
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
			var conID = widget.midiConnectors.indexOf(connector);
				if (moreArgs[0] == conID) {
					all[widget].do { |nb|
						nb.view.value_(changer[conID].value.midiMean)
					}
				}
			})
		}
	}
}

SoftWithinNumberBox : SCViewHolder {
	classvar all, c = 0;
	var widget, mc;
	var connector, syncKey;

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
		this.set(index);
		this.prAddController;
	}

	// set the view to the specified connector's model value
	set { |connectorID|
		connector = widget.midiConnectors[connectorID];
		this.view.value_(mc.model[connectorID].value.softWithin)
	}

	close {
		this.viewDidClose;
		all[widget].remove(this);
		if (all[widget].isEmpty) {
			mc.controller.removeAt(syncKey);
			widget.prRemoveSyncKey(syncKey, true);
		}
	}

	prAddController {
		mc.controller !? {
			syncKey = (\softWithinNumberBox_ ++ c).asSymbol;
			c = c + 1;
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				var conID = widget.midiConnections.indexOf(connector);
				if (moreArgs[0] == conID) {
					all[widget].do { |nb|
						nb.view.value_(changer[conID].value.softWithin)
					}
				}
			})
		}
	}
}

MidiResolutionNumberBox : SCViewHolder {
	classvar all, c = 0;
	var widget, mc;
	var connector;

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
		this.set(index);
		this.prAddController;
	}

	// set the view to the specified connector's model value
	set { |connectorID|
		connector = widget.midiConnectors[connectorID];
		this.view.value_(mc.model[connectorID].value.midiResolution)
	}

	close {
		this.viewDidClose;
		all[widget].remove(this);
		if (all[widget].isEmpty) {
			mc.controller.removeAt(syncKey);
			widget.prRemoveSyncKey(syncKey, true);
		}
	}

	prAddController {
		mc.controller !? {
			syncKey = (\midiResolutionNumberBox_ ++ c).asSymbol;
			c = c + 1;
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				var conID = widget.midiConnectors.indexOf(connector);
				if (moreArgs[0] == conID) {
					all[widget].do { |nb|
						nb.view.value_(changer[conID].value.midiResolution)
					}
				}
			})
		}
	}
}

SlidersPerBankNumberTF : SCViewHolder {
	classvar all, c = 0;
	var widget, mc;
	var connector, syncKey;

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
		this.set(index);
		this.prAddController;
	}

	// set the view to the specified connector's model value
	set { |connectorID|
		connector = widget.midiConnectors[connectorID];
		this.view.string_(mc.model[connectorID].value.ctrlButtonBank)
	}

	close {
		this.viewDidClose;
		all[widget].remove(this);
		if (all[widget].isEmpty) {
			mc.controller.removeAt(syncKey);
			widget.prRemoveSyncKey(syncKey, true);
		}
	}

	prAddController {
		mc.controller !? {
			syncKey = (\slidersPerBankNumberBox_ ++ c).asSymbol;
			c = c + 1;
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				var conID = widget.midiConnectors.indexOf(connector);
				if (moreArgs[0] == conID) {
					all[widget].do { |tf|
						tf.view.string_(changer[conID].value.ctrlButtonBank)
					}
				}
			})
		}
	}
}