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

		// this.view = parentView;
		widget = wdgt;
		mc = widget.wmc.midiDisplay;
		rect ?? { rect = Point(100, 20) };
		this.view = PopUpMenu(parentView)
		.items_(["Select connection..."] ++ widget.midiConnectors)
	}
}

MidiLearnButton : SCViewHolder {
	classvar all;
	var widget, mc, connectorID;

	*initClass {
		all = ();
	}

	*new { |parent, widget, connectorID=0, rect|
		^super.new.init(parent, widget, connectorID, rect);
	}

	init { |parentView, wdgt, index, rect|
		widget = wdgt;
		all[widget] ?? { all.put(widget, List[]) };
		all[widget].add(this);
		connectorID = index;
		// this.view = parentView;
		mc = widget.wmc.midiDisplay;
		rect ?? { rect = Point(20, 20) };
		this.view = Button(parentView, rect).states_([
			["L", Color.white, Color.blue],
			["X", Color.white, Color.red]
		]);
		this.view.value_(this.view.states.detectIndex { |s, i|
			s[0] == mc.model[connectorID].value.learn
		});
		this.prAddController;
	}

	set { |index|
		connectorID = index;
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
			mc.controller.removeAt(\midiLearnButton);
			widget.prRemoveSyncKey(\midiLearnButton, true);
		}
	}

	prAddController {
		mc.controller !? {
			widget.prAddSyncKey(\midiLearnButton, true);
			mc.controller.put(\midiLearnButton, { |changer, what ... moreArgs|
				var pos;

				if (moreArgs[0] == connectorID) {
					all[widget].do { |view|
						pos = this.view.states.detectIndex { |a, i|
							a[0] == changer[connectorID].value.learn
						};
						this.view.value_(pos);
					}
				}
			})
		}
	}}

MidiSrcSelect : SCViewHolder {
	classvar all;
	var widget, mc, connectorID;

	*initClass {
		all = ();
	}

	*new { |parent, widget, connectorID, rect|
		^super.new.init(parent, widget, connectorID, rect);
	}

	init { |parentView, wdgt, index, rect|
		all[wdgt] ?? { all[wdgt] = List[] };
		all[wdgt].add(this);
		if (index.isNil) {	connectorID = 0 } { connectorID = index };

		widget = wdgt;
		mc = widget.wmc.midiDisplay;
		rect ?? { rect = Point(120, 20) };
		this.view = PopUpMenu(parentView, rect).items_(["source"]);
		this.view.item_(
			this.view.items.detectIndex(_ == mc.model[connectorID].value.src)
		);
		// TODO: add dependency to MIDI inititialisation -> fill items with list of sources
		this.prAddController;
	}

	set { |index|
		connectorID = index;
		this.view.item_(this.view.items.detectIndex(_ == mc.model[connectorID].value.src));
	}

	close {
		this.viewDidClose;
		all[widget].remove(this);
		if (all[widget].isEmpty) {
			mc.controller.removeAt(\midiSrcField);
			widget.prRemoveSyncKey(\midiSrcField, true);
		}
	}

	prAddController {
		mc.controller !? {
			widget.prAddSyncKey(\midiSrcField, true);
			mc.controller.put(\midiSrcField, { |changer, what ... moreArgs|
				if (moreArgs[0] == connectorID) {
					all[widget].do { |view|
						view.value_(changer[connectorID].value.src)
					}
				}
			})
		}
	}
}

MidiChanField : SCViewHolder {
	classvar all;
	var widget, mc, connectorID;

	*initClass {
		all = ();
	}

	*new { |parent, widget, connectorID, rect|
		^super.new.init(parent, widget, connectorID, rect);
	}

	init { |parentView, wdgt, index, rect|
		all[wdgt] ?? { all[wdgt] = List[] };
		all[wdgt].add(this);
		if (index.isNil) {	connectorID = 0 } { connectorID = index };

		widget = wdgt;
		mc = widget.wmc.midiDisplay;
		rect ?? { rect = Point(120, 20) };
		this.view = TextField(parentView, rect).string_(
			mc.model[connectorID].value.chan
		);
		this.prAddController;
	}

	set { |index|
		connectorID = index;
		this.view.string_(mc.model[connectorID].value.chan);
	}

	close {
		this.viewDidClose;
		all[widget].remove(this);
		if (all[widget].isEmpty) {
			mc.controller.removeAt(\midiChanField);
			widget.prRemoveSyncKey(\midiChanField, true);
		}
	}

	prAddController {
		mc.controller !? {
			widget.prAddSyncKey(\midiChanField, true);
			mc.controller.put(\midiChanField, { |changer, what ... moreArgs|
				if (moreArgs[0] == connectorID) {
					all[widget].do { |view|
						view.string_(changer[connectorID].value.chan)
					}
				}
			})
		}
	}
}

MidiCtrlField : SCViewHolder {
	classvar all;
	var widget, mc, connectorID;

	*initClass {
		all = ();
	}

	*new { |parent, widget, connectorID, rect|
		^super.new.init(parent, widget, connectorID, rect);
	}

	init { |parentView, wdgt, index, rect|
		all[wdgt] ?? { all[wdgt] = List[] };
		all[wdgt].add(this);
		if (index.isNil) {	connectorID = 0 } { connectorID = index };

		widget = wdgt;
		mc = widget.wmc.midiDisplay;
		rect ?? { rect = Point(120, 20) };
		this.view = TextField(parentView, rect).string_(
			mc.model[connectorID].value.ctrl
		);
		this.prAddController;
	}

	set { |index|
		connectorID = index;
		this.view.string_(mc.model[connectorID].value.ctrl);
	}

	close {
		this.viewDidClose;
		all[widget].remove(this);
		if (all[widget].isEmpty) {
			mc.controller.removeAt(\midiCtrlField);
			widget.prRemoveSyncKey(\midiCtrlField, true);
		}
	}

	prAddController {
		mc.controller !? {
			widget.prAddSyncKey(\midiCtrlField, true);
			mc.controller.put(\midiCtrlField, { |changer, what ... moreArgs|
				if (moreArgs[0] == connectorID) {
					all[widget].do { |view|
						view.string_(changer[connectorID].value.ctrl)
					}
				}
			})
		}
	}
}

MidiModeSelect : SCViewHolder {
	classvar all;
	var widget, mc, connectorID;

	*initClass {
		all = ();
	}

	*new { |parent, widget, connectorID, rect|
		^super.new.init(parent, widget, connectorID, rect);
	}

	init { |parentView, wdgt, index, rect|
		all[wdgt] ?? { all[wdgt] = List[] };
		all[wdgt].add(this);
		if (index.isNil) {	connectorID = 0 } { connectorID = index };

		widget = wdgt;
		mc = widget.wmc.midiOptions;
		rect ?? { rect = Point(120, 20) };
		this.view = PopUpMenu(parentView, rect).items_(["0-127", "+/-"])
		.value_(mc.model[connectorID].value.midiMode);
		this.prAddController;
	}

	set { |index|
		connectorID = index;
		this.view.value_(mc.model[connectorID].value.midiMode)
	}

	close {
		this.viewDidClose;
		all[widget].remove(this);
		if (all[widget].isEmpty) {
			mc.controller.removeAt(\midiModeSelect);
			widget.prRemoveSyncKey(\midiModeSelect, true);
		}
	}

	prAddController {
		mc.controller !? {
			widget.prAddSyncKey(\midiModeSelect, true);
			mc.controller.put(\midiModeSelect, { |changer, what ... moreArgs|
				if (moreArgs[0] == connectorID) {
					all[widget].do { |view|
						view.value_(changer[connectorID].value.midiMode)
					}
				}
			})
		}
	}
}

MidiMeanNumberBox : SCViewHolder {
	classvar all;
	var widget, mc, <connectorID;

	*initClass {
		all = ();
	}

	*new { |parent, widget, connectorID, rect|
		^super.new.init(parent, widget, connectorID, rect);
	}

	init { |parentView, wdgt, index, rect|
		all[wdgt] ?? { all[wdgt] = List[] };
		all[wdgt].add(this);
		if (index.isNil) {	connectorID = 0 } { connectorID = index };

		widget = wdgt;
		mc = widget.wmc.midiOptions;
		rect ?? { rect = Point(120, 20) };
		this.view = NumberBox(parentView, rect).value_(mc.model[connectorID].value.midiMean);
		this.prAddController;
	}

	set { |index|
		connectorID = index;
		this.view.value_(mc.model[connectorID].value.midiMean)
	}

	close {
		this.viewDidClose;
		all[widget].remove(this);
		if (all[widget].isEmpty) {
			mc.controller.removeAt(\midiMeanNumberBox);
			widget.prRemoveSyncKey(\midiMeanNumberBox, true);
		}
	}

	prAddController {
		mc.controller !? {
			widget.prAddSyncKey(\midiMeanNumberBox, true);
			mc.controller.put(\midiMeanNumberBox, { |changer, what ... moreArgs|
				if (moreArgs[0] == connectorID) {
					all[widget].do { |view|
						view.value_(changer[connectorID].value.midiMean)
					}
				}
			})
		}
	}
}

SoftWithinNumberBox : SCViewHolder {
	classvar all;
	var widget, mc, connectorID;

	*initClass {
		all = ();
	}

	*new { |parent, widget, connectorID, rect|
		^super.new.init(parent, widget, connectorID, rect);
	}

	init { |parentView, wdgt, index, rect|
		all[wdgt] ?? { all[wdgt] = List[] };
		all[wdgt].add(this);
		if (index.isNil) {	connectorID = 0 } { connectorID = index };

		widget = wdgt;
		mc = widget.wmc.midiOptions;
		rect ?? { rect = Point(120, 20) };
		this.view = NumberBox(parentView, rect).value_(mc.model[connectorID].value.softWithin);
		this.prAddController;
	}

	set { |index|
		connectorID = index;
		this.view.value_(mc.model[connectorID].value.softWithin)
	}

	close {
		this.viewDidClose;
		all[widget].remove(this);
		if (all[widget].isEmpty) {
			mc.controller.removeAt(\softWithinNumberBox);
			widget.prRemoveSyncKey(\softWithinNumberBox, true);
		}
	}

	prAddController {
		mc.controller !? {
			widget.prAddSyncKey(\softWithinNumberBox, true);
			mc.controller.put(\softWithinNumberBox, { |changer, what ... moreArgs|
				if (moreArgs[0] == connectorID) {
					all[widget].do { |view|
						view.value_(changer[connectorID].value.softWithin)
					}
				}
			})
		}
	}
}

MidiResolutionNumberBox : SCViewHolder {
	classvar <all;
	var widget, mc, connectorID;

	*initClass {
		all = ();
	}

	*new { |parent, widget, connectorID, rect|
		^super.new.init(parent, widget, connectorID, rect);
	}

	init { |parentView, wdgt, index, rect|
		all[wdgt] ?? { all[wdgt] = List[] };
		all[wdgt].add(this);
		if (index.isNil) {	connectorID = 0 } { connectorID = index };

		widget = wdgt;
		mc = widget.wmc.midiOptions;
		rect ?? { rect = Point(120, 20) };
		this.view = NumberBox(parentView, rect).value_(mc.model[connectorID].value.midiResolution);
		this.prAddController;
	}

	set { |index|
		connectorID = index;
		this.view.value_(mc.model[connectorID].value.midiResolution)
	}

	close {
		this.viewDidClose;
		all[widget].remove(this);
		if (all[widget].isEmpty) {
			mc.controller.removeAt(\midiResolutionNumberBox);
			widget.prRemoveSyncKey(\midiResolutionNumberBox, true);
		}
	}

	prAddController {
		mc.controller !? {
			widget.prAddSyncKey(\midiResolutionNumberBox, true);
			mc.controller.put(\midiResolutionNumberBox, { |changer, what ... moreArgs|
				if (moreArgs[0] == connectorID) {
					all[widget].do { |view|
						view.value_(changer[connectorID].value.midiResolution)
					}
				}
			})
		}
	}
}

SlidersPerBankNumberTF : SCViewHolder {
	classvar all;
	var widget, mc, connectorID;

	*initClass {
		all = ();
	}

	*new { |parent, widget, connectorID, rect|
		^super.new.init(parent, widget, connectorID, rect);
	}

	init { |parentView, wdgt, index, rect|
		all[wdgt] ?? { all[wdgt] = List[] };
		all[wdgt].add(this);
		if (index.isNil) {	connectorID = 0 } { connectorID = index };

		widget = wdgt;
		mc = widget.wmc.midiOptions;
		rect ?? { rect = Point(120, 20) };
		this.view = TextField(parentView, rect).string_(mc.model[connectorID].value.ctrlButtonBank);
		this.prAddController;
	}

	set { |index|
		connectorID = index;
		this.view.string_(mc.model[connectorID].value.ctrlButtonBank)
	}

	close {
		this.viewDidClose;
		all[widget].remove(this);
		if (all[widget].isEmpty) {
			mc.controller.removeAt(\slidersPerBankNumberBox);
			widget.prRemoveSyncKey(\slidersPerBankNumberBox, true);
		}
	}

	prAddController {
		mc.controller !? {
			widget.prAddSyncKey(\slidersPerBankNumberBox, true);
			mc.controller.put(\slidersPerBankNumberBox, { |changer, what ... moreArgs|
				if (moreArgs[0] == connectorID) {
					all[widget].do { |view|
						view.string_(changer[connectorID].value.ctrlButtonBank)
					}
				}
			})
		}
	}
}