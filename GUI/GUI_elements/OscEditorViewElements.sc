// OSC Editors

OscCmdNameField : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;
	var <connector, <widget;
	var connections;
	var validOsc = "^/[\\w\\d\\H/]+[\\w\\d\\H]+[^/\\h]$";

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
		var action, conID;

		widget = wdgt;
		all[widget] ?? { all[widget] = List[] };
		all[widget].add(this);

		mc = widget.wmc.oscDisplay;
		conModel = widget.oscConnectors;
		connections = widget.wmc.oscConnections;

		this.view = TextField(parentView, rect);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.enabled_(connections.m.value[index].isNil);
		action = { |tf|
			conID = connector.index;
			this.connector.setOscCmdName(tf.string);
			if (tf.string.asSymbol !== '/path/to/cmd') {
				if (tf.string.size > 0) {
					mc.m.value[conID].learn = false;
					if (validOsc.matchRegexp(tf.string)) {
						// "textfield string matching".postln;
						mc.m.value[conID].connectState = ["connect", Color.white, Color.blue];
						mc.m.value[conID].connectEnabled = true;
						mc.m.value[conID].connectWarning = nil;
					} {
						// "textfield string not matching".postln;
						mc.m.value[conID].connectState = ["connect", Color.white, Color.gray];
						mc.m.value[conID].connectEnabled = false;
						mc.m.value[conID].connectWarning = "The given OSC message is invalid: OSC messages must begin with a slash and must not contain spaces."
					}
				} {
					mc.m.value[conID].learn = true;
					mc.m.value[conID].connectState = ["learn", Color.yellow, Color.green(0.5)];
					mc.m.value[conID].connectEnabled = true;
					mc.m.value[conID].connectWarning = nil;
				};
				mc.m.changedPerformKeys(widget.syncKeys, conID);
			};
		};
		this.view.focusLostAction_(action);
		this.view.action_(action);
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
		this.view.string_(mc.m.value[connectorID].nameField)
		.enabled_(connections.m.value[connectorID].isNil);
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
		connections = widget.wmc.oscConnections;
		this.view.enabled_(connections.m.value[0].isNil);
		this.index_(0);
		// oscConnector at index 0 should always exist (who knows...)
		this.prAddController;
	}

	prAddController {
		var conID;
		mc.c ?? {
			mc.c = SimpleController(mc.m)
		};
		connections.c ?? {
			connections.c = SimpleController(connections.m)
		};
		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		mc.c.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget].do { |tf|
				if (tf.connector === conModel[conID]) {
					// mc.m.value[conID].learn = "connect";
					defer {
						tf.view.string_(changer.value[conID].nameField);
						tf.view.toolTip_(changer.value[conID].connectWarning);
					}
				}
			}
		});
		connections.c.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget].do { |tf|
				if (tf.connector === conModel[conID]) {
					defer { tf.view.enabled_(changer.value[conID].isNil) }
				}
			}
		})
	}
}

// TODO: rename to OscMasgIndexBox
OscMsgIndexBox : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;
	var <connector, <widget;
	var connections;

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
		connections = widget.wmc.oscConnections;

		this.view = NumberBox(parentView, rect)
		.clipLo_(1).step_(1).scroll_step_(1)
		.toolTip_("If OSC message conatains more than one value select message slot that shall be read");
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.enabled_(connections.m.value[index].isNil);
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
		this.view.value_(mc.m.value[connectorID].index)
		.enabled_(connections.m.value[connectorID].isNil);
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
		connections = widget.wmc.oscConnections;
		this.view.enabled_(connections.m.value[0].isNil);
		this.index_(0);
		// oscConnector at index 0 should always exist (who knows...)
		this.prAddController;
	}

	prAddController {
		var conID;
		mc.c ?? {
			mc.c = SimpleController(mc.m)
		};
		connections.c ?? {
			connections.c = SimpleController(connections.c)
		};
		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		mc.c.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget].do { |nb|
				if (nb.connector === conModel[conID]) {
					defer {
						nb.view.clipHi_(changer.value[conID].numMsgSlots)
						.value_(changer.value[conID].index)
					}
				}
			}
		});
		connections.c.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget].do { |nb|
				if (nb.connector === conModel[conID]) {
					defer { nb.view.enabled_(changer.value[conID].isNil )}
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
					defer { nb.view.value_(changer.value[conID].oscEndless) }
				}
			}
		})
	}
}

OscMatchingCheckBox : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;
	var <connector, <widget;
	var connections;

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
		connections = widget.wmc.oscConnections;

		this.view = CheckBox(parentView, rect)
		.toolTip_("Create \"matching\" OSCFunc");
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.enabled_(connections.m.value[index].isNil);
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
		};
		this.view.enabled_(connections.m.value[connectorID].isNil);
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
		connections = widget.wmc.oscConnections;
		this.view.enabled_(connections.m.value[0].isNil);
		this.index_(0);
		// oscConnector at index 0 should always exist (who knows...)
		this.prAddController;
	}

	prAddController {
		var conID;
		mc.c ?? {
			mc.c = SimpleController(mc.m)
		};
		connections.c ?? {
			connections.c = SimpleController(connections.m)
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
		});
		connections.c.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget].do { |cb|
				if (cb.connector === conModel[conID]) {
					defer { cb.view.enabled_(changer.value[conID].isNil )}
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

OscSnapDistanceNumBox : ConnectorElementView {
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

		this.view = NumberBox(parentView, rect).step_(0.1).scroll_step_(0.1).clipLo_(0.0).clipHi_(1.0);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |nb|
			this.connector.setOscSnapDistance(nb.value);
		});
		connectorRemovedFuncAdded ?? {
			OscConnector.onConnectorRemove_({ |widget, id|
				this.prOnRemoveConnector(widget, id)
			});
			connectorRemovedFuncAdded = true
		};
		this.prAddController;
	}

	// set the view to the specified connector's model value
	index_ { |connectorID|
		connector = conModel[connectorID];
		mc.m.value[connectorID] !? {
			this.view.value_(connector.getOscSnapDistance)
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
					defer { nb.view.value_(changer.value[conID].oscSnapDistance) }
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

		this.view = StaticText(parentView, rect).string_("0.0").minWidth_(30)
		.toolTip_("input zero-crossing correction");
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
	var validOsc = "^/[\\w\\d\\H/]+[\\w\\d\\H]+[^/\\h]$";

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
		var ip, port, addr, cmd, cmdIndex, matching, argTemplate, dispatcher;
		var conID;

		widget = wdgt;
		all[widget] ?? { all[widget] = List[] };
		all[widget].add(this);

		mc = widget.wmc;
		conModel = widget.oscConnectors;

		this.view = Button(parentView, rect)
		.states_([mc.oscDisplay.m.value[index].connectState]);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |bt|
			conID = connector.index;
			// "mc.oscConnections.m.value[%].isNil? %".format(conID, mc.oscConnections.m.value[conID].isNil).postln;
			if (mc.oscConnections.m.value[conID].isNil) {
				if (mc.oscDisplay.m.value[conID].ipField.notNil) {
					ip = mc.oscDisplay.m.value[conID].ipField.asString
				} { ip = nil };
				mc.oscDisplay.m.value[conID].portField !? { port = mc.oscDisplay.m.value[conID].portField };
				cmd = this.connector.getOscCmdName;
				cmdIndex = this.connector.getOscMsgIndex;
				matching = this.connector.getOscMatching;
				argTemplate = this.connector.getOscTemplate;
				dispatcher = this.connector.getOscDispatcher;
				if (mc.oscDisplay.m.value[conID].learn) {
					// "\n% (%): should learn".format(conID, widget.oscConnectors.indexOf(this.connector)).postln;
					mc.oscDisplay.m.value[conID].learn = false;
					mc.oscDisplay.m.changedPerformKeys(widget.syncKeys, conID);
					OSCFunc.cvWidgetLearn(widget, conID, matching, NetAddr.langPort, argTemplate, dispatcher);
				} {
					// "\n%: connecting, addr: %, cmd: %, cmdIndex: %".format(widget.oscConnectors.indexOf(this.connector), addr, cmd, cmdIndex).postln;
					this.connector.oscConnect(addr, cmd, cmdIndex, NetAddr.langPort, argTemplate, dispatcher, matching);
					mc.oscDisplay.m.value[conID].connectState = ["disconnect", Color.white, Color.red];
					mc.oscDisplay.m.changedPerformKeys(widget.syncKeys, conID);
				}
			} {
				this.connector.oscDisconnect;
				mc.oscDisplay.m.value[conID].connectState = ["connect", Color.white, Color.blue];
				mc.oscDisplay.m.changedPerformKeys(widget.syncKeys, conID);
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
		// mc.oscDisplay.m.value.do { |v, i|
		// 	"\n%:".format(i).warn;
		// 	v.pairsDo { |n, m|
		// 		"%: %".format(n, m).postln;
		// 	}
		// };
		// mc.oscConnections.m.value.do { |v, i|
		// 	"%: %".format(i, v).warn;
		// };
		this.view.states_([mc.oscDisplay.m.value[connectorID].connectState])
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
		conModel = widget.oscConnectors;
		defaultState = mc.oscDisplay.m.value[0].connect0;
		this.index_(0);
		this.prAddController;
	}

	prAddController {
		var pos, conID, state0;

		mc.oscOptions.c ?? {
			mc.oscOptions.c = SimpleController(mc.oscOptions.m)
		};
		mc.oscDisplay.c ?? {
			mc.oscDisplay.c = SimpleController(mc.oscDisplay.m)
		};
		mc.oscConnections.c ?? {
			mc.oscConnections.c = SimpleController(mc.oscConnections.m)
		};
		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		mc.oscOptions.c.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget].do { |bt, i|
				if (bt.connector === conModel[conID]) {
					// "oscOptions controller: % (connector ID: %)".format(changer.value[conID], conID).postln
				}
			}
		});
		mc.oscDisplay.c.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget].do { |bt, i|
				if (bt.connector === conModel[conID]) {
					defer {
						bt.view.states_([changer.value[conID].connectState]);
						bt.view.enabled_(changer.value[conID].connectEnabled);
						bt.view.toolTip_(changer.value[conID].connectWarning);
					}
				}
			}
		});
		mc.oscConnections.c.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget].do { |bt, i|
				if (bt.connector === conModel[conID]) {
					defer { bt.view.value_(changer.value[conID].notNil.asInteger) }
				}
			}
		})
	}
}