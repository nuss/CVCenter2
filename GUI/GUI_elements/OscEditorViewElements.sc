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
		isConnected = widget.wmc.oscConnections.m.value[index];

		this.view = TextField(parentView, rect)
		.enabled_(isConnected.not);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_( { |tf|
			this.connector.name_(tf.string.asSymbol)
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
		this.view.enabled_(widget.wmc.oscConnections.m.value[0].not);
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
						tf.view.enabled_(widget.wmc.oscConnections.m.value[conID].not)
					}
				}
			}
		})
	}
}

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
		isConnected = widget.wmc.oscConnections.m.value[index];

		this.view = NumberBox(parentView, rect)
		.clipLo_(1).step_(1).scroll_step_(1)
		.enabled_(isConnected.not);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |nb|
			var i = conModel.indexOf(this.connector);
			mc.m.value[i].index = nb.value;
			mc.m.changedPerformKeys(widget.syncKeys, i);
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

		mc = widget.wmc.oscDisplay;
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
		"position: %, index: %, widget.wmc.oscInputConstrainters: %".format(position, index, widget.wmc.oscInputConstrainters).postln;
		"position: %, widget.wmc.oscInputConstrainter[%]: %".format(position, index, widget.wmc.oscInputConstrainters[index]).postln;
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

		this.view = StaticText(parentView, rect).string_("0.0");
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