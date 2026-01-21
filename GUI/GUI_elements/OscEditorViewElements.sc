// OSC Editors

OscCmdNameField : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;
	var <connector, <widget;
	var isConnected;

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
		conModel = widget.oscConnectors;
		isConnected = widget.wmc.oscConnections.m.value[index].notNil;

		this.view = TextField(parentView, rect)
		.enabled_(isConnected.not);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_( { |tf|
			this.connector.setOscCmdName(tf.string)
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
		connector = conModel[connectorID];
		this.view.string_(mc.m.value[connectorID].nameField);
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
		mc = widget.wmc.oscDisplay;
		conModel = widget.oscConnectors;
		this.view.enabled_(widget.wmc.oscConnections.m.value[0].notNil);
		this.index_(0);
		// oscConnector at index 0 should always exist (who knows...)
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
					defer {
						tf.view.string_(changer.value[conID].nameField);
						tf.view.enabled_(widget.wmc.oscConnections.m.value[conID].notNil)
					}
				}
			}
		})
	}
}

// TODO: rename to OscMasgIndexBox
OscCmdIndexBox : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;
	var <connector, <widget;
	var isConnected;

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
		conModel = widget.oscConnectors;
		isConnected = widget.wmc.oscConnections.m.value[index].notNil;

		this.view = NumberBox(parentView, rect)
		.clipLo_(1).step_(1).scroll_step_(1)
		.enabled_(isConnected.not);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |nb|
			this.connector.setOscMsgIndex(nb.value)
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
		connector = conModel[connectorID];
		this.view.value_(mc.m.value[connectorID].index);
	}

	widget_ { |otherWidget|
		if (otherWidget.class !== CVWidgetKnob) {
			Error("Widget must be a CVWidgetKnob").throw
		};

		all[otherWidget] ?? { all[otherWidget] = List[] };
		all[otherWidget].add(this);
		this.prCleanup;
		// switch after cleanup has finished
		widget = otherWidget;
		mc = widget.wmc.oscDisplay;
		conModel = widget.oscConnectors;
		this.index_(0);
		// midiConnector at index 0 should always exist (who knows...)
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
			all[widget].do { |nb|
				if (nb.connector === conModel[conID]) {
					defer { nb.view.value_(changer.value[conID].index) }
				}
			}
		})
	}
}

OscModeSelect : ConnectorElementView {
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

		mc = widget.wmc.oscOptions;
		conModel = widget.oscConnectors;

		this.view = PopUpMenu(parentView, rect)
		.items_(['absolute', 'endless']);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |nb|
			this.connector.setOscEndless(nb.value.asBoolean)
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
		connector = conModel[connectorID];
		this.view.value_(mc.m.value[connectorID].oscEndless);
	}


	widget_ { |otherWidget|
		if (otherWidget.class !== CVWidgetKnob) {
			Error("Widget must be a CVWidgetKnob").throw
		};

		all[otherWidget] ?? { all[otherWidget] = List[] };
		all[otherWidget].add(this);
		this.prCleanup;
		// switch after cleanup has finished
		widget = otherWidget;
		mc = widget.wmc.oscOptions;
		conModel = widget.oscConnectors;
		this.index_(0);
		// midiConnector at index 0 should always exist (who knows...)
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
			all[widget].do { |nb|
				if (nb.connector === conModel[conID]) {
					defer { nb.view.value_(changer.value[conID].oscEndless) }
				}
			}
		})
	}
}

OscMatchingCheckBox : ConnectorElementView {
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

		mc = widget.wmc.oscOptions;
		conModel = widget.oscConnectors;

		this.view = CheckBox(parentView, rect);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |cb|
			this.connector.setOscMatching(cb.value)
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
		connector = conModel[connectorID];
		mc.m.value[connectorID] !? {
			this.view.value_(connector.getOscMatching)
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
		mc = widget.wmc.oscOptions;
		conModel = widget.oscConnectors;
		this.index_(0);
		// oscConnector at index 0 should always exist (who knows...)
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
			all[widget].do { |nb|
				if (nb.connector === conModel[conID]) {
					defer { nb.view.value_(changer.value[conID].oscMatching) }
				}
			}
		})
	}
}

OscResolutionBox : ConnectorElementView {
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

		mc = widget.wmc.oscOptions;
		conModel = widget.oscConnectors;

		this.view = NumberBox(parentView, rect).clipLo_(0).scroll_step_(0.1).step_(0.1);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_( { |nb|
			this.connector.setOscResolution(nb.value)
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
		connector = conModel[connectorID];
		mc.m.value[connectorID] !? {
			this.view.value_(connector.getOscResolution)
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
		mc = widget.wmc.oscOptions;
		conModel = widget.oscConnectors;
		this.index_(0);
		// oscConnector at index 0 should always exist (who knows...)
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
			all[widget].do { |nb|
				if (nb.connector === conModel[conID]) {
					defer { nb.view.value_(changer.value[conID].oscResolution) }
				}
			}
		})
	}
}

OscConstrainterNumBox : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;
	var <connector, <widget, cv, position;

	*initClass {
		all = ()
	}

	*new { |parent, widget, rect, connectorID=0, position|
		if (widget.isKindOf(CVWidget).not) {
			Error("arg 'widget' must be a kind of CVWidget").throw
		};
		^super.new.init(parent, widget, rect, connectorID, position)
	}

	init { |parentView, wdgt, rect, index, pos|
		widget = wdgt;
		position = pos;
		all[widget] ?? { all[widget] = List[] };
		all[widget].add(this);

		mc = widget.wmc.oscDisplay;
		cv = switch(position)
		{ 0 } { widget.wmc.oscInputConstrainters[index].lo }
		{ 1 } { widget.wmc.oscInputConstrainters[index].hi };
		conModel = widget.oscConnectors;

		this.view = NumberBox(parentView, rect);
		cv.connect(this.view);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |nb|
			switch(position)
			{ 0 } {
				this.connector.setOscInputConstraints([
					nb.value, this.connector.getOscInputConstraints[1]
				])
			}
			{ 1 } {
				this.connector.setOscInputConstraints([
					this.connector.getOscInputConstraints[0], nb.value
				])
			}
		});
		connectorRemovedFuncAdded ?? {
			OscConnector.onConnectorRemove_({ |widget, id|
				this.prOnRemoveConnector(widget, id)
			});
			connectorRemovedFuncAdded = true
		};
		// this.prAddController;
	}

	index_ { |connectorID|
		connector = conModel[connectorID];
		mc.m.value[connectorID] !? {
			cv.disconnect(this.view);
			cv = switch(position)
			{ 0 } { widget.wmc.oscInputConstrainters[connectorID].lo }
			{ 1 } { widget.wmc.oscInputConstrainters[connectorID].hi };
			cv.connect(this.view);
		}
	}

	widget_ { |otherWidget|
		// FIXME: check for CVWidget2D slot (once it's implemented...)
		if (otherWidget.class !== CVWidgetKnob) {
			Error("Widget must be a CVWidgetKnob").throw
		};

		all[otherWidget] ?? { all[otherWidget] = List[] };
		all[otherWidget].add(this);
		cv.disconnect(this.view);
		this.prCleanup;
		mc = widget.wmc.oscOptions;
		conModel = widget.oscConnectors;
		this.index_(0);
		// oscConnector at index 0 should always exist (who knows...)
		this.prAddController;
		cv = switch(position)
		{ 0 } { widget.wmc.oscInputConstrainters[0].lo }
		{ 1 } { widget.wmc.oscInputConstrainters[0].hi };
		cv.connect(this.view);
		// this.prAddController;
	}

	/* prAddController {
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
			all[widget].do { |nb|
				if (nb.connector === conModel[conID]) {
					defer { nb.view.value_(changer.value[conID].oscResolution) }
				}
			}
		})
	} */
}

OscZeroCrossingText : ConnectorElementView {
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
		conModel = widget.oscConnectors;

		this.view = StaticText(parentView, rect).string_("0.0").minWidth_(30);
		this.view.onClose_({ this.close });
		this.index_(index);
		connectorRemovedFuncAdded ?? {
			OscConnector.onConnectorRemove_({ |widget, id|
				this.prOnRemoveConnector(widget, id)
			});
			connectorRemovedFuncAdded = true
		};
		this.prAddController;
	}

	index_ { |connectorID|
		connector = conModel[connectorID]
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
		mc = widget.wmc.oscDisplay;
		conModel = widget.oscConnectors;
		this.index_(0);
		// oscConnector at index 0 should always exist (who knows...)
		this.prAddController;
	}

	prAddController {
		// TODO
	}
}

OscCalibrationButton : ConnectorElementView {
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

		mc = widget.wmc.oscOptions;
		conModel = widget.oscConnectors;

		this.view = Button(parentView, rect)
		.states_([
			["calibrate", Color.white, Color.red],
			["calibrating", Color.black, Color.green]
		]);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_( { |bt|
			this.connector.setOscCalibration(bt.value.asBoolean)
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
		connector = conModel[connectorID];
		mc.m.value[connectorID] !? {
			this.view.value_(connector.getOscCalibration.asInteger)
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
		mc = widget.wmc.oscOptions;
		conModel = widget.oscConnectors;
		this.index_(0);
		// oscConnector at index 0 should always exist (who knows...)
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
			all[widget].do { |bt|
				if (bt.connector === conModel[conID]) {
					defer { bt.view.value_(changer.value[conID].oscCalibration.asInteger) }
				}
			}
		})
	}
}

OscCalibrationResetButton : ConnectorElementView {
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

		mc = widget.wmc.oscOptions;
		conModel = widget.oscConnectors;

		this.view = Button(parentView, rect)
		.states_([
			["reset", Color.black, Color(0.9, 0.7, 0.14)]
		]);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_( { |bt|
			this.connector.resetOscCalibration
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
		connector = conModel[connectorID];
		mc.m.value[connectorID] !? {
			this.view.value_(connector.getOscCalibration.asInteger)
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
		mc = widget.wmc.oscOptions;
		conModel = widget.oscConnectors;
		this.index_(0);
		// oscConnector at index 0 should always exist (who knows...)
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
			all[widget].do { |bt|
				if (bt.connector === conModel[conID]) {
					defer { bt.view.value_(changer.value[conID].oscCalibration.asInteger) }
				}
			}
		})
	}
}

OscConnectButton : ConnectorElementView {
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
		var defaultState;

		widget = wdgt;
		all[widget] ?? { all[widget] = List[] };
		all[widget].add(this);

		mc = widget.wmc;
		conModel = widget.oscConnectors;

		case
		{ mc.oscDisplay.m.value[index].nameField === '/my/cmd/name' or: {
			"^/[\\w\\d\\H/]+[\\w\\d\\H]+[^/\\h]$".matchRegexp(mc.oscDisplay.m.value[index].nameField.asString).not
		}} {
			defaultState = ["learn", Color.white, Color.blue]
		}
		// check https://www.boost.org/doc/libs/1_69_0/libs/regex/doc/html/boost_regex/syntax/perl_syntax.html
		{ "^/[\\w\\d\\H/]+[\\w\\d\\H]+[^/\\h]$".matchRegexp(mc.oscDisplay.m.value[index].nameField.asString) } {
			defaultState = ["connect", Color.black, Color.green]
		};

		this.view = Button(parentView, rect)
		.states_([
			defaultState,
			["disconnect", Color.white, Color.red]
		]);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |bt|
			var i = conModel.indexOf(this.connector);
			var ip, port, cmd, cmdIndex, matching;
			mc.oscDisplay.m.value[i].connect = bt.states[bt.value][0];
			mc.oscDisplay.m.changedPerformKeys(widget.syncKeys, i);
			if (mc.oscDisplay.m.value[i].connect == "disconnect") {
				mc.oscDisplay.m.value[i].ipField !? { ip = mc.oscDisplay.m.value[i].ipField };
				mc.oscDisplay.m.value[i].portField !? { port = mc.oscDisplay.m.value[i].portField };
				if (mc.oscDisplay.m.value[i].nameField != '/my/cmd/name' and: {
					mc.oscDisplay.m.value[i].asString.size > 0
				}) {
					cmd = mc.oscDisplay.m.value[i].nameField
				};
				cmdIndex = mc.oscDisplay.m.value[i].index;
				matching = mc.oscOptions.m.value[i].matching;
				this.connector.oscConnect(ip, port, cmd, cmdIndex, matching);
				if (ip.notNil or: { port.notNil or: { cmd.notNil }}) {
					all[widget].do { |b|
						if (conModel.indexOf(b.connector) == i) {
							b.view.states_([
								["learn", Color.white, Color.blue],
								["disconnect", Color.white, Color.red]
							])
						}
					}
				}
			}
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
		connector = conModel[connectorID];
		mc.oscDisplay.m.value[connectorID] !? {
			mc.oscDisplay.m.value[connectorID].learn.switch(
				"disconnect", { this.view.value_(1) },
				"learn", { this.view.value_(0) }
			)
		}
	}

	widget_ { |otherWidget|
		var defaultState;

		// FIXME: check for CVWidget2D slot (once it's implemented...)
		if (otherWidget.class !== CVWidgetKnob) {
			Error("Widget must be a CVWidgetKnob").throw
		};

		all[otherWidget] ?? { all[otherWidget] = List[] };
		all[otherWidget].add(this);
		this.prCleanup;
		// switch after cleanup has finished
		widget = otherWidget;
		mc = widget.wmc;
		conModel = widget.midiConnectors;
		if (mc.oscDisplay.m.value[0].learn == "connect") {
			defaultState = ["connect", Color.black, Color.green];
		} {
			defaultState = ["learn", Color.white, Color.blue];
		};
		this.index_(0);
		this.prAddController;
	}

	prAddController {
		var pos, conID;
		mc.oscOptions.c ?? {
			mc.oscOptions.c = SimpleController(mc.oscOptions.m)
		};
		mc.oscDisplay.c ?? {
			mc.oscDisplay.c = SimpleController(mc.oscDisplay.m)
		};
		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		mc.oscOptions.c.put(\syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget].do { |bt, i|
				if (bt.connector === conModel[conID]) {
					"oscOptions controller: % (connector ID: %)".format(changer.value[conID], conID).postln
				}
			}
		});
		mc.oscDisplay.c.put(\syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget].do { |bt, i|
				if (bt.connector === conModel[conID]) {
					"oscDisplay controller: % (connector ID: %)".format(changer.value[conID], conID).postln
				}
			}
		})
	}
}