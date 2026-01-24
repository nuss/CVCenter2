OscSelectsComboView : CompositeView {
	classvar <all, connectorRemovedFuncAdded;
	var wmc, osc, oscDisplay, states, connectors, connections, syncKey;
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
		connectors = widget.oscConnectors;
		connections = widget.wmc.oscConnections;

		if (parentView.isNil) {
			parent = Window("%: OSC addresses and commands".format(widget.name), Rect(0, 0, 300, 65));
		} {
			parent = parentView;
		};

		this.background_(Color(red: 0.5, green: 0.5, alpha: 0.5)).minHeight_(80);

		e = ();
		e.ipselect = PopUpMenu(parent).items_(['IP address...'] ++ osc.m.value.keys.asArray.sort)
		.toolTip_("Optionally select an IP address").minHeight_(25);
		e.colon = StaticText(parent).string_($:);
		e.portselect = PopUpMenu(parent).items_(['port...'])
		.toolTip_("Optionally select a port").minHeight_(25);
		// "e.portselect.items %".format(e.portselect.items).warn;
		e.cmdselect = PopUpMenu(parent).items_(['cmd name...'])
		.toolTip_("Select command name to be listened to").minHeight_(25);
		e.scanbut = Button(parent).states_([
			["scan OSC", Color.white, Color(green: 0.5, blue: 0.5)],
			["stop  scan", Color.white, Color.red]
		])
		.maxWidth_(80).minHeight_(25)
		.toolTip_("Scan for incoming OSC messages");
		e.rreset = Button(parent).states_([
			["reset", Color.white, Color(red: 1.0, green: 0.5)]
		])
		.maxWidth_(50).minHeight_(25)
		.toolTip_("Reset all IP addresses, ports and command names");

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
			OSCCommands.collectSync(bt.value.asBoolean);
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
				osc.m.value[e.ipselect.item] = nil;
			}
			{ e.ipselect.value > 0 and: { e.portselect.value > 0 }} {
				osc.m.value[e.ipselect.item][e.portselect.item.asSymbol] = nil;
			}
			{
				osc.m.value_(());
			};
			osc.m.changedPerformKeys(CVWidget.syncKeys)
		});

		connectorRemovedFuncAdded ?? {
			OscConnector.onConnectorRemove_({ |widget, id|
				this.prOnRemoveConnector(widget, id)
			});
			connectorRemovedFuncAdded = true
		};

		this.prAddController;
	}

	index_ { |connectorID|
		var ipId, portId, cmdId;
		var cmds;

		connector = connectors[connectorID];
		if (oscDisplay.m.value[connectorID].ipField.isNil) {
			e.ipselect.value_(0);
			e.portselect.items_([e.portselect.items[0]]).value_(0);
			cmds = [];
			osc.m.value.deepCollect(2, { |k| cmds = cmds ++ k.keys });
			e.cmdselect.items_([e.cmdselect.items[0]] ++ cmds.asSet.asArray.sort).value_(0)
		} {
			ipId = e.ipselect.items.indexOf(oscDisplay.m.value[connectorID].ipField);
			e.ipselect.value_(ipId);
			e.portselect.items_([e.portselect.items[0]] ++ osc.m.value[e.ipselect.item].keys.asArray.collect(_.asInteger).sort);
			if (oscDisplay.m.value[connectorID].portField.notNil) {
				portId = e.portselect.items.indexOf(oscDisplay.m.value[connectorID].portField);
				e.portselect.value_(portId);
			} {
				e.portselect.value_(0)
			};
			if (oscDisplay.m.value[connectorID].portField.isNil) {
				// select index of command across all port values
				cmds = osc.m.value[e.ipselect.item].atAll(osc.m.value[e.ipselect.item].keys).asArray.collect { |pairs| pairs.keys.asArray }.flat.sort;
			} {
				// select index of command in values under given port
				cmds = osc.m.value[e.ipselect.item][oscDisplay.m.value[connectorID].portField.asSymbol].keys.asArray.sort;
			};
			e.cmdselect.items_([e.cmdselect.items[0]] ++ cmds);
			e.cmdselect.value_(e.cmdselect.items.indexOf(oscDisplay.m.value[connectorID].nameField))
		}
	}

	widget_ { |otherWidget|
		// FIXME: check for CVWidget2D slot (once it's implemented...)
		if (otherWidget.class !== CVWidgetKnob) {
			Error("Widget must be a CVWidgetKnob").throw
		};

		all[otherWidget] ?? { all[otherWidget] = List[] };
		all[otherWidget].add(this);

		this.prCleanup;
		// switch after cleanup has finished
		widget = otherWidget;

		wmc = CVWidget.wmc;
		osc = wmc.oscAddrAndCmds;
		oscDisplay = widget.wmc.oscDisplay;
		connectors = widget.oscConnectors;

		if (oscDisplay.m.value[0].ipField.isNil) {
			e.ipselect.value_(0)
		} {
			e.ipselect.value_(e.ipselect.items.indexOf(oscDisplay.m.value[0].ipField));
			if (oscDisplay.m.value[0].portField.isNil) {
				e.portselect.value_(0)
			} {
				e.portselect.value_(e.portselect.items.indexOf(oscDisplay.m.value[0].portField))
			}
		};
		e.cmdselect.value_(e.cmdselect.items.indexOf(oscDisplay.m.value[0].nameField));

		this.index_(0);
		this.prAddController;
	}

	enabled_ { |boolEnabled|
		[e.ipselect, e.portselect, e.cmdselect].do(_.enabled_(boolEnabled));
	}

	enabled {
		^e.ipselect.enabled && e.portselect.enabled && e.cmdselect.enabled
	}

	prAddController {
		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		CVWidget.syncKeys.indexOf(syncKey) ?? {
			CVWidget.prAddSyncKey(syncKey, true)
		};
		this.prAddIsScanningOscController(syncKey);
		this.prAddOscAddrAndCmdsController(syncKey);
		this.prAddOscDisplayController(syncKey);
		this.prAddOscConnectionsController(syncKey);
	}

	prAddIsScanningOscController { |syncKey|
		wmc.isScanningOsc.c ?? {
			wmc.isScanningOsc.c = SimpleController(wmc.isScanningOsc.m)
		};
		wmc.isScanningOsc.c.put(syncKey, { |changer, what ... moreArgs|
			all.do { |comboList|
				comboList.do { |combo|
					defer {
						combo.e.scanbut.value_(changer.value.asInteger)
					}
				}
			}
		})
	}

	prAddOscAddrAndCmdsController { |syncKey|
		var ips, cmds;

		osc.c ?? { osc.c = SimpleController(osc.m) };
		osc.c.put(syncKey, { |changer, what ... moreArgs|
			if (changer.value.isEmpty) {
				all.do { |comboList|
					comboList.do { |combo|
						defer {
							combo.e.ipselect.items_([e.ipselect.items[0]]);
							combo.e.portselect.items_([e.portselect.items[0]]);
							combo.e.cmdselect.items_([e.cmdselect.items[0]]);
						}
					}
				}
			} {
				ips = changer.value.keys.asArray.sort;
				all.do { |comboList|
					comboList.do { |combo|
						defer {
							combo.e.ipselect.items_([e.ipselect.items[0]] ++ ips);
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
								// "ipselect and portselect value > 0".warn;
								cmds = osc.m.value[combo.e.ipselect.item][combo.e.portselect.item].keys.asArray.sort;
								combo.e.cmdselect.items_([combo.e.cmdselect.items[0]] ++ cmds)
							}
						}
					}
				}
			}
		})
	}

	prAddOscDisplayController { |syncKey|
		var conID, cmds, ip, cmdIndex, port;

		oscDisplay.c ?? { oscDisplay.c = SimpleController(oscDisplay.m) };
		oscDisplay.c.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			// "changer: %".format(changer.value[conID]).postln;
			all[widget].do { |selCombo|
				if (selCombo.connector === connectors[conID]) {
					case
					{ changer.value[conID].ipField.isNil and: {
						changer.value[conID].portField.isNil
					}} {
						defer {
							selCombo.e.rreset.toolTip_("Reset all IP addresses, ports and command names");
							selCombo.e.ipselect.value_(0);
							selCombo.e.portselect.items_([selCombo.e.portselect.items[0]]).value_(0);
							cmds = [];
							osc.m.value.deepCollect(2, { |k| cmds = cmds ++ k.keys });
							selCombo.e.cmdselect.items_([selCombo.e.cmdselect.items[0]] ++ cmds.asSet.asArray.sort);
						}
					}
					{ changer.value[conID].ipField.notNil and: {
						changer.value[conID].portField.isNil
					}} {
						defer {
							ip = changer.value[conID].ipField;
							selCombo.e.rreset.toolTip_("Reset all ports and command names under IP %".format(ip));
							selCombo.e.ipselect.value_(selCombo.e.ipselect.items.indexOf(ip));
							selCombo.e.portselect.items_(
								[selCombo.e.portselect.items[0]] ++ osc.m.value[ip].keys.asArray.collect(_.asInteger).sort
							).value_(0);
							selCombo.e.cmdselect.items_(
								[selCombo.e.cmdselect.items[0]] ++ osc.m.value[ip].values.collect(_.keys).collect(_.asArray).flat.sort
							)
						}
					}
					{ changer.value[conID].ipField.notNil and: {
						changer.value[conID].portField.notNil
					}} {
						ip = changer.value[conID].ipField;
						port = changer.value[conID].portField;
						cmds = osc.m.value[changer.value[conID].ipField][changer.value[conID].portField.asSymbol].keys.asArray.sort;
						defer {
							selCombo.e.rreset.toolTip_("Reset all command names under IP:port %:%".format(ip, port));
							selCombo.e.ipselect.value_(selCombo.e.ipselect.items.indexOf(ip));
							// needed if value was 'learned'
							selCombo.e.portselect.items.indexOf(changer.value[conID].portField) ?? {
								selCombo.e.portselect.items_(selCombo.e.portselect.items ++ changer.value[conID].portField)
							};
							selCombo.e.portselect.value_(selCombo.e.portselect.items.indexOf(port));
							selCombo.e.cmdselect.items_([selCombo.e.cmdselect.items[0]] ++ cmds);
						}
					};
					defer {
						if (changer.value[conID].nameField !== '/path/to/cmd' and: {
							(cmdIndex = selCombo.e.cmdselect.items.indexOf(changer.value[conID].nameField)).notNil
						}) {
							selCombo.e.cmdselect.value_(cmdIndex)
						}
					}
				}
			}
		})
	}

	prAddOscConnectionsController { |syncKey|
		var conID;

		connections.c ?? {
			connections.c = SimpleController(connections.m);
		};
		connections.c.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget].do { |selCombo|
				if (selCombo.connector === connectors[conID]) {
					defer { selCombo.enabled_(changer.value[conID].isNil) }
				}
			}
		})
	}

	prOnRemoveConnector { |widget, index|
		try {
			if (index > 0) {
				all[widget].do(_.index_(index - 1))
			} {
				all[widget].do(_.index_(index))
			}
		}
	}

	close {
		this.remove;
		e.do(_.close);
		this.prCleanup;
	}

	prCleanup {
		all[widget].remove(this);
		try {
			if (all[widget].notNil and: { all[widget].isEmpty }) {
				oscDisplay.c.removeAt(syncKey);
				widget.prRemoveSyncKey(syncKey, true);
				all.removeAt(widget);
			};
			if (all.isEmpty) {
				wmc.isScanningOsc.c.removeAt(syncKey);
				osc.c.removeAt(syncKey);
				CVWidget.prRemoveSyncKey(syncKey, true);
			}
		}
	}
}