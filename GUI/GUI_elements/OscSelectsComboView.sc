OscSelectsComboView : CompositeView {
	classvar <all, connectorRemovedFuncAdded;
	var wmc, osc, oscDisplay, states, connectors, syncKey;
	var <e, <connector, <widget, i;

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

		wmc = CVWidget.wmc;
		osc = wmc.oscAddrAndCmds;
		oscDisplay = widget.wmc.oscDisplay;
		states = widget.wmc.oscSelectsStates;
		connectors = widget.wmc.oscConnectors.m.value;

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

		this.index_(index);

		e.scanbut.action_({ |bt|
			wmc.isScanningOsc.m.value_(bt.value.asBoolean).changedPerformKeys(CVWidget.syncKeys);
			if (bt.value == 0) {
				wmc.oscAddrAndCmds.m.value.putAll(OSCCommands.ipsAndCmds);
				wmc.oscAddrAndCmds.m.changedPerformKeys(CVWidget.syncKeys);
			} {
				OSCCommands.collect(bt.value.asBoolean);
			}
		});
		e.ipselect.action_({ |sel|
			i = connectors.indexOf(this.connector);
			if (sel.value == 0) {
				oscDisplay.m.value[i].ipField = nil;
				oscDisplay.m.value[i].portField = nil;
				states.m.value[i].ipSelect = 0;
			} {
				oscDisplay.m.value[i].ipField = sel.items[sel.value];
				states.m.value[i].ipSelect = sel.value;
			};
			oscDisplay.m.changedPerformKeys(widget.syncKeys, i);
			states.m.changesPerformKeys(widget.syncKeys, i);
		});
		e.portselect.action_({ |sel|
			i = connectors.indexOf(this.connector);
			if (sel.value == 0) {
				oscDisplay.m.value[i].portField = nil;
				states.m.value[i].portSelect = 0;
			} {
				oscDisplay.m.value[i].portField = sel.items[sel.value];
				states.m.value[i].portSelect = sel.value;
			};
			oscDisplay.m.changedPerformKeys(widget.syncKeys, i);
			states.m.changedPerformKeys(widget.syncKeys, i);
		});
		e.cmdselect.action_({ |sel|
			i = connectors.indexOf(this.connectors);
			if (sel.value > 0) {
				oscDisplay.m.value[i].nameField = sel.items[sel.value]
			};
			oscDisplay.m.changedPerformKeys(widget.syncKeys, i);
		});

		this.prAddController;
	}

	index_ { |connectorID|
		connector = connectors[connectorID];

	}

}