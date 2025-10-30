OscSelectsComboView : CompositeView {
	classvar <all, connectorRemovedFuncAdded;
	var wmc, osc, oscDisplay, states, connectors, syncKey;
	var <e, <connector, <widget, i;

	*initClass {
		all = ();
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
		all[widget] ?? { all[widget] = List[] };
		all[widget].add(this);

		wmc = CVWidget.wmc;
		osc = wmc.oscAddrAndCmds;
		oscDisplay = widget.wmc.oscDisplay;
		states = widget.wmc.oscSelectsStates;
		connectors = widget.oscConnectors;

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
		this.onClose_({ this.close });

		e.scanbut.action_({ |bt|
			wmc.isScanningOsc.m.value_(bt.value.asBoolean).changedPerformKeys(CVWidget.syncKeys);
			if (bt.value == 0) {
				wmc.oscAddrAndCmds.m.value.putAll(OSCCommands.ipsAndCmds);
				wmc.oscAddrAndCmds.m.changedPerformKeys(CVWidget.syncKeys);
			} {
				OSCCommands.collect(bt.value.asBoolean);
			};
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
			states.m.changedPerformKeys(widget.syncKeys, i);
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
		var ipId, portId, cmdId;
		var cmds;

		connector = connectors[connectorID];
		if (oscDisplay.m.value[connectorID].ipField.isNil) {
			e.ipselect.value_(0);
			e.portselect.items_(['select port... (optional)']).value_(0);
			e.cmdselect.items_(['select command name']).value_(0)
		} {
			ipId = e.ipselect.items.indexOf(oscDisplay.m.value[connectorID].ipField);
			e.ipselect.value_(ipId);
			e.portselect.items_(osc.m.value[e.ipselect.item].keys.asArray.sort);
			if (oscDisplay.m.value[connectorID].portField.notNil) {
				portId = e.portselect.items.indexOf(oscDisplay.m.value[connectorID].portField);
				e.portselect.value_(portId);
			} {
				e.portselect.value_(0)
			};
			if (oscDisplay.m.value[connectorID].nameField !== '/my/cmd/name') {
				if (oscDisplay.m.value[connectorID].portField.isNil) {
					// select index of command across all port values
					cmds = osc.m.value[e.ipselect.item].atAll(osc.m.value[e.ipselect.item].keys).asArray.flat.asSet.asArray.sort;
				} {
					// select index of command in values under given port
					cmds = osc.m.value[oscDisplay.m.value[connectorID].portField]
				};
				e.cmdselect.items_(['select command name'] ++ cmds);
			}
		}
	}

	widget_ { |otherWidget|

	}

	prAddController {
		var conID;
		var ips, ports, cmds;
		var ipsvals, portsvals;
		var ip, port;

		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		CVWidget.syncKeys.indexOf(syncKey) ?? {
			CVWidget.prAddSyncKey(syncKey, true)
		};
		wmc.isScanningOsc.c ?? {
			wmc.isScanningOsc.c = SimpleController(wmc.isScanningOsc.m)
		};
		wmc.isScanningOsc.c.put(syncKey, { |changer, what ... moreArgs|
			all.do { |comboList|
				comboList.do { |combo|
					combo.e.scanbut.value_(changer.value.asInteger)
				}
			}
		});
		osc.c ?? { osc.c = SimpleController(osc.m) };
		osc.c.put(syncKey, { |changer, what ... moreArgs|
			changer.value.postln;
			"CVWidget.wmc.oscAddrAndCmds: %".format(osc.m.value).postln;
			ips = changer.value.keys;
			all.do { |comboList|
				comboList.do { |combo|
					ipsvals = (combo.e.ipselect.items[1..].asSet ++ ips).asArray.sort;
					combo.e.ipselect.items_([e.ipselect.items[0]] ++ ipsvals);
				}
			}
		});
		oscDisplay.c ?? { oscDisplay.c = SimpleController(oscDisplay.m) };
		states.c ?? { states.c = SimpleController(states.m) };
		states.c.put(syncKey, { |changer, what ... moreArgs|
			[changer.value, moreArgs].postln;
			// populate portselect with ports available under the given IP
			// or empty list if no IP is given (position 0)
			// populate cmdselect with appropriate comd names
			// if neither IP nor port is selected add all available cmd names
			// otherwise restrict to IP or IP and port
		})
	}

	prOnRemoveConnector { |widget, index|

	}

	close {
		this.remove;
		e.do(_.close);
		this.prCleanup;
	}

	prCleanup {
		all[widget].remove(this);
		if (all[widget].isEmpty) {
			// remove controllers -> to be defined
			// mc.controller.removeAt(syncKey);
			widget.prRemoveSyncKey(syncKey, true);
		}
	}
}