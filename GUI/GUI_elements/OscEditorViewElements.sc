// OSC Editors

OscConnectorNameField : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;
	var <connector, <widget;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, connectorID=0|
		if (widget.isKindOf(CVWidget).not) {
			Error("arg 'widget' must be a kind of CVWidget").throw
		};
		^super.new.init(parent, widget, rect, connectorID);
	}

	init { |parentView, wdgt, rect, index|
		widget = wdgt;
		all[widget] ?? { all[widget] = List[] };
		all[widget].add(this);

		mc = widget.wmc.oscConnectorNames;
		conModel = widget.wmc.oscConnectors.m.value;

		this.view = TextField(parentView, rect);
		this.index_(index);
		this.view.action_({ |tf|
			this.connector.name_(tf.string.asSymbol)
		});
		this.view.onClose_({ this.close });
		this.prAddController;
	}

	index_ { |connectorID|
		connector = conModel[connectorID];
		mc.m.value !? {
			this.view.string_(mc.m.value[connectorID])
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
		mc = widget.wmc.oscConnectorNames;
		conModel = widget.wmc.oscConnectors.m.value;
		// oscConnector at index 0 should always exist (who knows...)
		this.index_(0);
		this.prAddController;
	}

	prAddController {
		var conID;
		mc.c ?? {
			mc.c = SimpleController(mc.m)
		};
		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		mc.c.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget].do { |tf|
				if (tf.connector === conModel[conID]) {
					tf.view.string_(changer.value[conID]);
				}
			}
		})
	}
}

OscConnectorSelect : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;
	var <connector, <widget;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, connectorID=0|
		if (widget.isKindOf(CVWidget).not) {
			Error("arg 'widget' must be a kind of CVWidget").throw
		};
		^super.new.init(parent, widget, rect, connectorID);
	}

	init { |parentView, wdgt, rect, index|
		widget = wdgt;
		all[widget] ?? { all[widget] = List[] };
		all[widget].add(this);

		mc = widget.wmc.oscConnectorNames;
		conModel = widget.wmc.oscConnectors.m.value;

		this.view = PopUpMenu(parentView)
		.items_(mc.m.value ++ ["add OscConnector..."]);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.prAddController;
	}

	index_ { |connectorID|
		connector = conModel[connectorID];
		this.view.value_(connectorID);
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
		mc = widget.wmc.oscConnectorNames;
		conModel = widget.wmc.oscConnectors.m.value;
		this.view.items_(mc.m.value ++ this.view.items.last);
		// midiConnector at index 0 should always exist (who knows...)
		this.index_(0);
		this.prAddController;
	}

	prAddController {
		var items, conID;
		var curValue;
		mc.c ?? {
			mc.c = SimpleController(mc.m)
		};
		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		mc.c.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget].do { |sel, i|
				items = sel.view.items;
				items[conID] = changer.value[conID];
				curValue = sel.view.value;
				sel.view.items_(items).value_(curValue);
				if (sel.connector === conModel[conID]) {
					sel.view.value_(conID)
				}
			}
		})
	}
}

OscScanButton : ConnectorElementView {
	classvar <all;
	var wmc;

	*initClass {
		all = List[];
	}

	*new { |parent, rect|
		^super.new.init(parent, rect)
	}

	init { |parentView, rect|
		all.add(this);
		wmc = CVWidget.wmc;

		this.view = Button(parentView, rect)
		.states_([
			["start OSC scan", Color.black, Color.cyan],
			["stop OSC scan", Color.white, Color.red]
		])
		.action_({ |bt|
			OSCCommands.collectIPsAndCmds(bt.value.asBoolean);
			wmc.isScanningOsc.m.value_(bt.value.asBoolean).changedPerformKeys(CVWidget.syncKeys);
			if (bt.value == 0) {
				wmc.oscDevices.m.value.putAll(OSCCommands.ipsAndCmds).changedPerformKeys(CVWidget.syncKeys)
			}
		});
		this.view.onClose_({ this.close });
		this.prAddController;
	}

	index_ {}

	widget_ {}

	prAddController {
		wmc.isScanningOsc.c ?? {
			wmc.isScanningOsc.c = SimpleController(wmc.isScanningOsc.m)
		};
		wmc.oscDevices.c ?? {
			wmc.isScanningOsc.c = SimpleController(wmc.oscDevices.m)
		};
		syncKey = this.class.asSymbol;
		CVWidget.syncKeys.indexOf(syncKey) ?? {
			CVWidget.syncKeys.prAddSyncKey(syncKey, true);
			wmc.isScanningOsc.c.put(syncKey, { |changer, what|
				all.do { |bt|
					bt.view.value_(changer.asInteger)
				}
			});
			wmc.oscDevices.c.put(syncKey, { |changer, what|
				// TODO: should update list addresses in OscAddrSelect
				// probably in controller within OscAddrSelect
			})
		}
	}
}

OscAddrSelect : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;
	var <connector, <widget, wmc;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, connectorID=0|
		if (widget.isKindOf(CVWidget).not) {
			Error("arg 'widget' must be a kind of CVWidget").throw
		};
		^super.new.init(parent, widget, rect, connectorID);
	}

	init { |parentView, wdgt, rect, index|
		widget = wdgt;
		all[widget] ?? { all[widget] = List[] };
		all[widget].add(this);

		mc = widget.wmc.oscDisplay;
		conModel = widget.wmc.oscConnectors.m.value;
		wmc = CVWidget.wmc;
		this.view = PopUpMenu(parentView)
		.items_(wmc.oscDevices.m.value.keys.asArray.sort ++ ["select IP address... (optional)"]);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |sel|
			// TODO: set command select according to selected IP, probably in controller
		});
		this.prAddController;
	}

	index_ { |connectorID|
		var display;

		connector = conModel[connectorID];
		// mc.m.value[connectorID] !? {
			// TODO: should be set to IP of current connection if it exists?
			// otherwise set it to view.items.last
	// }
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
		mc = widget.wmc.oscConnectorNames;
		conModel = widget.wmc.oscConnectors.m.value;
		// midiConnector at index 0 should always exist (who knows...)
		this.index_(0);
		this.prAddController;
	}

	prAddController {
		var items, conID;
		mc.c ?? {
			mc.c = SimpleController(mc.m)
		};
		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		mc.c.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
		})
	}
}

AddPortRadioButton : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;
	var <connector, <widget;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, connectorID=0|
		if (widget.isKindOf(CVWidget).not) {
			Error("arg 'widget' must be a kind of CVWidget").throw
		};
		^super.new.init(parent, widget, rect, connectorID);
	}

	init { |parentView, wdgt, rect, index|
		widget = wdgt;
		all[widget] ?? { all[widget] = List[] };
		all[widget].add(this);

		mc = widget.wmc.oscDisplay;
		this.index_(index);
		this.prAddController;
	}

	index_ { |connectorID|
		connector = widget.oscConnectors[connectorID];
		mc.m.value[connectorID] !? {
			// this.view.items.indexOf()n
		}
	}

	widget_ { |otherWidget|

	}

	prAddController {

	}
}


