OscAddrSelectView : CompositeView {
	classvar <all, connectorRemovedFuncAdded;
	var osc, mc, conModel, connectors, syncKey;
	var <e, <connector, <widget;

	*initClass {
		all = List[];
	}

	*new { |parent, widget, rect, connectorID=0, layout([[\ipselect, \portselect], [\cmdselect, \scanbut]])|
		if (widget.isKindOf(CVWidget).not) {
			Error("arg 'widget' must be a kind of CVWidget").throw
		};
		^super.new.init(parent, widget, rect, connectorID, layout);
	}

	init { |parentView, wdgt, rect, index, layout|
		var parent, row, i;

		widget = wdgt;
		all.add(this);

		osc = CVWidget.wmc.oscAddrAndCmds;
		mc = widget.wmc.oscDisplay;
		conModel = widget.wmc.oscConnectors.m.valuel;

		if (parentView.isNil) {
			parent = Window("%: OSC addresses and commands".format(widget.name), Rect(0, 0, 300, 65));
		} {
			parent = parentView;
		};

		e = ();
		e.ipselect = PopUpMenu(parent).items_(['select IP address... (optional)'] ++ osc.m.value.keys.asArray.sort);
		e.portselect = PopUpMenu(parent).items_(['select port... (optional)']);
		e.cmdselect = PopUpMenu(parent).items_(['select command name']);
		e.scanbut = Button(parent).states_([
			["start OSC scan", Color.white, Color(green: 0.5, blue: 0.5)],
			["stop OSC scan", Color.white, Color.red]
		]);

		if (layout.size > 1) {
			this.layout_(VLayout());
			layout.size.do { |i|
				row = HLayout();
				layout[i].do { |k| row.add(e[k]) };
				this.layout.add(row)
			};
		} {
			row = HLayout();
			layout[0].do { |k| row.add(e[k]) };
			this.layout.add(row)
		};
	}

}