MidiLearnButton : SCViewHolder {
	classvar all;
	var widget, mc;

	*new { |parent, widget, rect|
		^super.new.init(parent, widget, rect);
	}

	init { |parentView, wdgt, rect|
		all ?? { all = () };
		all[wdgt] ?? { all[wdgt] = List[] };
		all[wdgt].add(this);

		// this.view = parentView;
		widget = wdgt;
		mc = widget.wmc.midi;
		rect ?? { rect = Point(20, 20) };
		this.view = Button(parentView, rect).states_([
			["L", Color.white, Color.blue],
			["X", Color.white, Color.red]
		]);
	}

	set { |connection|
		mc[connection].value.learn.switch(
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
	var widget, mc;

	*new { |parent, widget, rect|
		^super.new.init(parent, widget, rect);
	}

	init { |parentView, wdgt, rect|
		all ?? { all = () };
		all[wdgt] ?? { all[wdgt] = List[] };
		all[wdgt].add(this);

		widget = wdgt;
		mc = widget.wmc.midi;
		rect ?? { rect = Point(120, 20) };
		this.view = TextField(parentView, rect).string_("source").canFocus_(false);
	}

	set { |connection|
		this.view.string_(mc[connection].value.src);
	}
}

MidiChanField : SCViewHolder {
	classvar all;
	var widget, mc;

	*new { |parent, widget, rect|
		^super.new.init(parent, widget, rect);
	}

	init { |parentView, wdgt, rect|
		all ?? { all = () };
		all[wdgt] ?? { all[wdgt] = List[] };
		all[wdgt].add(this);

		widget = wdgt;
		mc = widget.wmc.midi;
		rect ?? { rect = Point(120, 20) };
		this.view = TextField(parentView, rect).string_("chan").canFocus_(false);
	}

	set { |connection|
		this.view.string_(mc[connection].value.chan);
	}
}

MidiCtrlField : SCViewHolder {
	classvar all;
	var widget, mc;

	*new { |parent, widget, rect|
		^super.new.init(parent, widget, rect);
	}

	init { |parentView, wdgt, rect|
		all ?? { all = () };
		all[wdgt] ?? { all[wdgt] = List[] };
		all[wdgt].add(this);

		widget = wdgt;
		mc = widget.wmc.midi;
		rect ?? { rect = Point(120, 20) };
		this.view = TextField(parentView, rect).string_("ctrl").canFocus_(false);
	}

	set { |connection|
		this.view.string_(mc[connection].value.ctrl);
	}
}