OscSelectsComboView : CompositeView {
	classvar <all, connectorRemovedFuncAdded;
	var wmc, osc, oscDisplay, states, connectors, syncKey;
	var <e, <connector, <widget, i;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, connectorID=0, layout([[(name: \ipselect, stretch: 20), (name: \colon, stretch: 0), (name: \portselect, stretch: 10)], [(name: \cmdselect), (name: \scanbut), (name: \rreset)]])|
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
		e.ipselect = PopUpMenu(parent).items_(['IP address...'] ++ osc.m.value.keys.asArray.sort)
		.toolTip_("Optionally select an IP address");
		e.colon = StaticText(parent).string_($:);
		e.portselect = PopUpMenu(parent).items_(['port...'])
		.toolTip_("Optionally select a port");
		e.cmdselect = PopUpMenu(parent).items_(['cmd name...'])
		.toolTip_("Select command name to be listened to");
		e.scanbut = Button(parent).states_([
			["scan OSC", Color.white, Color(green: 0.5, blue: 0.5)],
			["stop  scan", Color.white, Color.red]
		])
		.maxWidth_(80)
		.toolTip_("Scan for incoming OSC messages");
		e.rreset = Button(parent).states_([
			["reset", Color.white, Color(red: 1.0, green: 0.5)]
		])
		.maxWidth_(50)
		.toolTip_("Reset IP addresses, ports and command names");

		if (layout.size > 1) {
			this.layout_(VLayout());
			layout.size.do { |i|
				row = HLayout();
				layout[i].do { |it| row.add(e[it.name], it.stretch) };
				this.layout.add(row)
			};
		} {
			row = HLayout();
			layout[0].do { |it| row.add(e[it.name], it.stretch) };
			this.layout.add(row)
		};

		this.index_(index);
		this.onClose_({ this.close });

		e.scanbut.action_({ |bt|
			wmc.isScanningOsc.m.value_(bt.value.asBoolean).changedPerformKeys(CVWidget.syncKeys);
			if (bt.value == 0) {
				osc.m.value.putAll(OSCCommands.ipsAndCmds);
				osc.m.changedPerformKeys(CVWidget.syncKeys);
			} {
				OSCCommands.collect(bt.value.asBoolean);
			};
		});
		e.ipselect.action_({ |sel|
			i = connectors.indexOf(this.connector);
			if (sel.value == 0) {
				oscDisplay.m.value[i].ipField = nil;
			} {
				oscDisplay.m.value[i].ipField = sel.items[sel.value];
			};
			// important! Otherwise port will not be found under given IP
			oscDisplay.m.value[i].portField = nil;
			oscDisplay.m.changedPerformKeys(widget.syncKeys, i);
		});
		e.portselect.action_({ |sel|
			i = connectors.indexOf(this.connector);
			if (sel.value == 0) {
				oscDisplay.m.value[i].portField = nil;
			} {
				oscDisplay.m.value[i].portField = sel.items[sel.value];
			};
			oscDisplay.m.changedPerformKeys(widget.syncKeys, i);
		});
		e.cmdselect.action_({ |sel|
			i = connectors.indexOf(this.connector);
			if (sel.value > 0) {
				oscDisplay.m.value[i].nameField = sel.items[sel.value]
			};
			oscDisplay.m.changedPerformKeys(widget.syncKeys, i);
		});
		e.rreset.action_({ |bt|
			case
			{ e.ipselect.value > 0 and: { e.portselect.value == 0 }} {
				OSCCommands.ipsAndCmds[e.ipselect.item] = nil;
				osc.m.value[e.ipselect.item] = nil;
			}
			{ e.ipselect.value > 0 and: { e.portselect.value > 0 }} {
				OSCCommands.ipsAndCmds[e.ipselect.item][e.portselect.item] = nil;
				osc.m.value[e.ipselect.item][e.portselect.item] = nil;
			}
			{
				OSCCommands.ipsAndCmds.clear;
				osc.m.value_(());
			};
			osc.m.changedPerformKeys(CVWidget.syncKeys)
		});

		this.prAddController;
	}

	index_ { |connectorID|
		var ipId, portId, cmdId;
		var cmds;

		connector = connectors[connectorID];
		if (oscDisplay.m.value[connectorID].ipField.isNil) {
			e.ipselect.value_(0);
			e.portselect.items_([e.portselect.items[0]]).value_(0);
			e.cmdselect.items_([e.cmdselect.items[0]]).value_(0)
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
				e.cmdselect.items_([e.cmdselect.items[0]] ++ cmds);
			}
		}
	}

	widget_ { |otherWidget|

	}

	prAddController {
		var conID;
		var ips, ports, cmds;
		var ipsvals, portsvals, cmdsvals;
		var ip, cmdIndex, port, val;

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
			// "changer: %".format(changer.value).postln;
			if (changer.value.isEmpty) {
				all.do { |comboList|
					comboList.do { |combo|
						combo.e.ipselect.items_([e.ipselect.items[0]]);
						combo.e.portselect.items_([e.portselect.items[0]]);
						combo.e.cmdselect.items_([e.cmdselect.items[0]]);
					}
				}
			} {
				ips = changer.value.keys;
				all.do { |comboList|
					comboList.do { |combo|
						ipsvals = (combo.e.ipselect.items[1..].asSet ++ ips).asArray.sort;
						combo.e.ipselect.items_([e.ipselect.items[0]] ++ ipsvals);
						// [combo.widget, combo.connector].postln;
						case
						{ combo.e.ipselect.value == 0 } {
							combo.e.portselect.value_(0);
							cmds = [];
							osc.m.value.deepCollect(2, { |k| cmds = cmds ++ k.keys });
							combo.e.cmdselect.items_([combo.e.cmdselect.items[0]] ++ cmds.asSet.asArray.sort)
						}
						// TODO: test me...
						// e = (a: (aa: (aa1: 1, aa2: 1), ab: (ab1: 1, ab2: 1)), b: (bb: (b1: 1, b2: 1)))
						{ combo.e.ipselect.value > 0 and: { combo.e.portselect.value == 0 }} {
							cmds = osc.m.value[combo.e.ipselect.item].values.collect { |k| k.keys.asArray }.flat;
							combo.e.cmdselect.items_([combo.e.cmdselect.items[0]] ++ cmds.asSet.asArray.sort)
						}
						// TODO: test me...
						{ combo.e.ipselect.value > 0 and: { combo.e.portselect.value > 0 }} {
							cmds = osc.m.value[combo.e.ipselect.item][combo.e.portselect.item].keys.asArray.sort;
							combo.e.cmdselect.items_([combo.e.cmdselect.items[0]] ++ cmds)
						}
					}
				}
			}
		});
		oscDisplay.c ?? { oscDisplay.c = SimpleController(oscDisplay.m) };
		oscDisplay.c.put(syncKey, { |changer, what ... moreArgs|
			// [changer.value, moreArgs].postln;
			conID = moreArgs[0];
			all[widget].do { |selCombo|
				if (selCombo.connector === connectors[conID]) {
					case
					{ changer.value[conID].ipField.isNil and: {
						changer.value[conID].portField.isNil
					}} {
						// "ipField and portField are nil".postln;
						selCombo.e.ipselect.value_(0);
						selCombo.e.portselect.items_([selCombo.e.portselect.items[0]]).value_(0);
						cmds = [];
						osc.m.value.deepCollect(2, { |k| cmds = cmds ++ k.keys });
						selCombo.e.cmdselect.items_([selCombo.e.cmdselect.items[0]] ++ cmds.asSet.asArray.sort);
					}
					{ changer.value[conID].ipField.notNil and: {
						changer.value[conID].portField.isNil
					}} {
						// "ipField is not nil but portField is".postln;
						ip = changer.value[conID].ipField;
						selCombo.e.ipselect.value_(selCombo.e.ipselect.items.indexOf(ip));
						// "osc.m.value['%'].keys.asArray.collect(_.asInteger).sort: %".format(ip, osc.m.value[ip].keys.asArray.collect(_.asInteger).sort).postln;
						selCombo.e.portselect.items_(
							[selCombo.e.portselect.items[0]] ++ osc.m.value[ip].keys.asArray.collect(_.asInteger).sort
						).value_(0);
						// "osc.m.value['%'].values.collect(_.keys).collect(_.asArray).flat.sort: %".format(ip, osc.m.value[ip].values.collect(_.keys).collect(_.asArray).flat.sort).postln;
						selCombo.e.cmdselect.items_(
							[selCombo.e.cmdselect.items[0]] ++ osc.m.value[ip].values.collect(_.keys).collect(_.asArray).flat.sort
						)
					}
					{ changer.value[conID].ipField.notNil and: {
						changer.value[conID].portField.notNil
					}} {
						// "neither ipField nor portField are nil".postln;
						ip = changer.value[conID].ipField;
						port = changer.value[conID].portField;
						selCombo.e.ipselect.value_(selCombo.e.ipselect.items.indexOf(ip));
						selCombo.e.portselect.value_(selCombo.e.portselect.items.indexOf(port));
						cmds = osc.m.value[changer.value[conID].ipField][changer.value[conID].portField.asSymbol].keys.asArray.sort;
						selCombo.e.cmdselect.items_([selCombo.e.cmdselect.items[0]] ++ cmds)
					};
					if (changer.value[conID].nameField !== '/my/cmd/name' and: {
						(cmdIndex = selCombo.e.cmdselect.items.indexOf(changer.value[conID].nameField)).notNil
					}) {
						selCombo.e.cmdselect.value_(cmdIndex)
					}
				}
			}
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