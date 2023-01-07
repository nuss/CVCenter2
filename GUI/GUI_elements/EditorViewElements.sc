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
		"mc: %".format(mc).postln;
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
}

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
		this.view = PopUpMenu(parentView, rect).items_(["source"]).canFocus_(false).item_(
			this.view.items.detectIndex(_ == mc.model[index].value.src)
		);
		// TODO: add dependency to MIDI inititialisation -> fill items with list of sources

	}

	set { |id|
		index = id;
		this.view.item_(this.view.items.detectIndex(_ == mc.model[index].value.src));
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
		).canFocus_(false);
	}

	set { |id|
		index = id;
		this.view.string_(mc.model[index].value.chan);
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
		).canFocus_(false);
	}

	set { |id|
		index = id;
		this.view.string_(mc.model[index].value.ctrl);
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
		.value_(mc.model[index].value.midiMode).canFocus_(false);
	}

	set { |id|
		index = id;
		this.view.value_(mc.model[index].value.midiMode)
	}
}

MidiMeanNumberBox : SCViewHolder {
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
		this.view = NumberBox(parentView, rect).value_(mc.model[index].value.midiMean).canFocus_(false);
	}

	set { |id|
		index = id;
		this.view.value_(mc.model[index].value.midiMean)
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
		this.view = NumberBox(parentView, rect).value_(mc.model[index].value.softWithin).canFocus_(false);
	}

	set { |id|
		index = id;
		this.view.value_(mc.model[index].value.softWithin)
	}
}

MidiResolutionNumberBox : SCViewHolder {
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
		this.view = NumberBox(parentView, rect).value_(mc.model[index].value.midiResolution).canFocus_(false);
	}

	set { |id|
		index = id;
		this.view.value_(mc.model[index].value.midiResolution)
	}
}

SlidersPerBankNumberBox : SCViewHolder {
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
		this.view = TextField(parentView, rect).string_(mc.model[index].value.ctrlButtonBank).canFocus_(false);
	}

	set { |id|
		index = id;
		this.view.string_(mc.model[index].value.ctrlButtonBank)
	}
}