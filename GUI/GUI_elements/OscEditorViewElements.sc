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
			var i = conModel.indexOf(this.connector);
			mc.m.value[i].nameField = tf.string;
			mc.m.value[i].connect = "Connect";
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


	}

	index_ { |connectorID|

	}


	widget_ { |otherWidget|

	}

	prAddController {

	}
}

OscResolutionBox : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;
	var <connector, <widget;

	*initClass {
		all = ();
	}

	*new {}

	init {}

	index_ {}

	widget_ {}

	prAddController {}
}