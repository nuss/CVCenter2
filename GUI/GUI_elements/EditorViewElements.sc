ConnectionSelect : SCViewHolder {
	classvar all;
	var widget, mc, index;

	*initClass {
		all = ();
	}

	*new { |parent, widget, index, rect|
		^super.new.init(parent, widget, index, rect);
	}

	init { |parentView, wdgt, id, rect|
		all[wdgt] ?? { all[wdgt] = List[] };
		all[wdgt].add(this);
		if (id.isNil) {	index = 0 } { index = id };

		// this.view = parentView;
		widget = wdgt;
		mc = widget.wmc.midiDisplay;
		rect ?? { rect = Point(100, 20) };
		this.view = PopUpMenu(parentView)
		.items_(["Select connection..."])
	}
}

MidiLearnButton : SCViewHolder {
	classvar all;
	var widget, mc, index;

	*initClass {
		all = ();
	}

	*new { |parent, widget, index, rect|
		^super.new.init(parent, widget, index, rect);
	}

	init { |parentView, wdgt, id, rect|
		all[wdgt] ?? { all[wdgt] = List[] };
		all[wdgt].add(this);
		if (id.isNil) {	index = 0 } { index = id };

		// this.view = parentView;
		widget = wdgt;
		mc = widget.wmc.midiDisplay;
		rect ?? { rect = Point(20, 20) };
		this.view = Button(parentView, rect).states_([
			["L", Color.white, Color.blue],
			["X", Color.white, Color.red]
		]).value_(this.view.states.detectIndex { |s, i|
			s[0][i] == mc.model[index].value.learn
		});
		this.prAddController;
	}

	set { |id|
		index = id;
		mc.model[index].value.learn.switch(
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
				if (moreArgs[0] == index) {
					all[widget].do { |view|
						view.value_(changer[index].value.learn)
					}
				}
			});
		}
	}}

MidiSrcField : SCViewHolder {
	classvar all;
	var widget, mc, index;

	*initClass {
		all = ();
	}

	*new { |parent, widget, index, rect|
		^super.new.init(parent, widget, index, rect);
	}

	init { |parentView, wdgt, id, rect|
		all[wdgt] ?? { all[wdgt] = List[] };
		all[wdgt].add(this);
		if (id.isNil) {	index = 0 } { index = id };

		widget = wdgt;
		mc = widget.wmc.midiDisplay;
		rect ?? { rect = Point(120, 20) };
		this.view = PopUpMenu(parentView, rect).items_(["source"]).item_(
			this.view.items.detectIndex(_ == mc.model[index].value.src)
		);
		// TODO: add dependency to MIDI inititialisation -> fill items with list of sources
		this.prAddController;
	}

	set { |id|
		index = id;
		this.view.item_(this.view.items.detectIndex(_ == mc.model[index].value.src));
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
				if (moreArgs[0] == index) {
					all[widget].do { |view|
						view.value_(changer[index].value.src)
					}
				}
			})
		}
	}
}

MidiChanField : SCViewHolder {
	classvar all;
	var widget, mc, index;

	*initClass {
		all = ();
	}

	*new { |parent, widget, index, rect|
		^super.new.init(parent, widget, index, rect);
	}

	init { |parentView, wdgt, id, rect|
		all[wdgt] ?? { all[wdgt] = List[] };
		all[wdgt].add(this);
		if (id.isNil) {	index = 0 } { index = id };

		widget = wdgt;
		mc = widget.wmc.midiDisplay;
		rect ?? { rect = Point(120, 20) };
		this.view = TextField(parentView, rect).string_(
			mc.model[index].value.chan
		);
		this.prAddController;
	}

	set { |id|
		index = id;
		this.view.string_(mc.model[index].value.chan);
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
				if (moreArgs[0] == index) {
					all[widget].do { |view|
						view.string_(changer[index].value.chan)
					}
				}
			})
		}
	}
}

MidiCtrlField : SCViewHolder {
	classvar all;
	var widget, mc, index;

	*initClass {
		all = ();
	}

	*new { |parent, widget, index, rect|
		^super.new.init(parent, widget, index, rect);
	}

	init { |parentView, wdgt, id, rect|
		all[wdgt] ?? { all[wdgt] = List[] };
		all[wdgt].add(this);
		if (id.isNil) {	index = 0 } { index = id };

		widget = wdgt;
		mc = widget.wmc.midiDisplay;
		rect ?? { rect = Point(120, 20) };
		this.view = TextField(parentView, rect).string_(
			mc.model[index].value.ctrl
		);
		this.prAddController;
	}

	set { |id|
		index = id;
		this.view.string_(mc.model[index].value.ctrl);
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
				if (moreArgs[0] == index) {
					all[widget].do { |view|
						view.string_(changer[index].value.ctrl)
					}
				}
			})
		}
	}
}

MidiModeSelect : SCViewHolder {
	classvar all;
	var widget, mc, index;

	*initClass {
		all = ();
	}

	*new { |parent, widget, index, rect|
		^super.new.init(parent, widget, index, rect);
	}

	init { |parentView, wdgt, id, rect|
		all[wdgt] ?? { all[wdgt] = List[] };
		all[wdgt].add(this);
		if (id.isNil) {	index = 0 } { index = id };

		widget = wdgt;
		mc = widget.wmc.midiOptions;
		rect ?? { rect = Point(120, 20) };
		this.view = PopUpMenu(parentView, rect).items_(["0-127", "+/-"])
		.value_(mc.model[index].value.midiMode);
		this.prAddController;
	}

	set { |id|
		index = id;
		this.view.value_(mc.model[index].value.midiMode)
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
				if (moreArgs[0] == index) {
					all[widget].do { |view|
						view.value_(changer[index].value.midiMode)
					}
				}
			})
		}
	}
}

MidiMeanNumberBox : SCViewHolder {
	classvar all;
	var widget, mc, <index;

	*initClass {
		all = ();
	}

	*new { |parent, widget, index, rect|
		^super.new.init(parent, widget, index, rect);
	}

	init { |parentView, wdgt, id, rect|
		all[wdgt] ?? { all[wdgt] = List[] };
		all[wdgt].add(this);
		if (id.isNil) {	index = 0 } { index = id };

		widget = wdgt;
		mc = widget.wmc.midiOptions;
		rect ?? { rect = Point(120, 20) };
		this.view = NumberBox(parentView, rect).value_(mc.model[index].value.midiMean);
		this.prAddController;
	}

	set { |id|
		index = id;
		this.view.value_(mc.model[index].value.midiMean)
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
				if (moreArgs[0] == index) {
					all[widget].do { |view|
						view.value_(changer[index].value.midiMean)
					}
				}
			})
		}
	}
}

SoftWithinNumberBox : SCViewHolder {
	classvar all;
	var widget, mc, index;

	*initClass {
		all = ();
	}

	*new { |parent, widget, index, rect|
		^super.new.init(parent, widget, index, rect);
	}

	init { |parentView, wdgt, id, rect|
		all[wdgt] ?? { all[wdgt] = List[] };
		all[wdgt].add(this);
		if (id.isNil) {	index = 0 } { index = id };

		widget = wdgt;
		mc = widget.wmc.midiOptions;
		rect ?? { rect = Point(120, 20) };
		this.view = NumberBox(parentView, rect).value_(mc.model[index].value.softWithin);
		this.prAddController;
	}

	set { |id|
		index = id;
		this.view.value_(mc.model[index].value.softWithin)
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
				if (moreArgs[0] == index) {
					all[widget].do { |view|
						view.value_(changer[index].value.softWithin)
					}
				}
			})
		}
	}
}

MidiResolutionNumberBox : SCViewHolder {
	classvar <all;
	var widget, mc, index;

	*initClass {
		all = ();
	}

	*new { |parent, widget, index, rect|
		^super.new.init(parent, widget, index, rect);
	}

	init { |parentView, wdgt, id, rect|
		all[wdgt] ?? { all[wdgt] = List[] };
		all[wdgt].add(this);
		if (id.isNil) {	index = 0 } { index = id };

		widget = wdgt;
		mc = widget.wmc.midiOptions;
		rect ?? { rect = Point(120, 20) };
		this.view = NumberBox(parentView, rect).value_(mc.model[index].value.midiResolution);
		this.prAddController;
	}

	set { |id|
		index = id;
		this.view.value_(mc.model[index].value.midiResolution)
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
				if (moreArgs[0] == index) {
					all[widget].do { |view|
						view.value_(changer[index].value.midiResolution)
					}
				}
			})
		}
	}
}

SlidersPerBankNumberTF : SCViewHolder {
	classvar all;
	var widget, mc, index;

	*initClass {
		all = ();
	}

	*new { |parent, widget, index, rect|
		^super.new.init(parent, widget, index, rect);
	}

	init { |parentView, wdgt, id, rect|
		all[wdgt] ?? { all[wdgt] = List[] };
		all[wdgt].add(this);
		if (id.isNil) {	index = 0 } { index = id };

		widget = wdgt;
		mc = widget.wmc.midiOptions;
		rect ?? { rect = Point(120, 20) };
		this.view = TextField(parentView, rect).string_(mc.model[index].value.ctrlButtonBank);
		this.prAddController;
	}

	set { |id|
		index = id;
		this.view.string_(mc.model[index].value.ctrlButtonBank)
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
				if (moreArgs[0] == index) {
					all[widget].do { |view|
						view.string_(changer[index].value.ctrlButtonBank)
					}
				}
			})
		}
	}
}