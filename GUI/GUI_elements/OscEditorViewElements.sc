// OSC Editors

OscConnectorNameField : ConnectorElementView {
	classvar <all;
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
		this.view = TextField(parentView, rect);
		this.index_(index);
		this.view.action_({ |tf|
			this.connector.name_(tf.string.asSymbol)
		});
		this.view.onClose_({ this.close });
		this.prAddController;
	}

	index_ { |connectorID|
		connector = widget.oscConnectors[connectorID];
		mc.model.value !? {
			this.view.string_(mc.model.value[connectorID])
		}
	}

	prAddController {
		var conID;
		mc.controller ?? {
			mc.controller = SimpleController(mc.model)
		};
		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				conID = moreArgs[0];
				all[widget].do { |tf|
					if (tf.connector === widget.oscConnectors[conID]) {
						tf.view.string_(changer.value[conID]);
					}
				}
			})
		}
	}
}

OscConnectorSelect : ConnectorElementView {
	classvar <all;
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
		this.view = PopUpMenu(parentView)
		.items_(widget.oscConnectors.collect(_.name) ++ ["add OscConnector..."]);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.prAddController;
	}

	index_ { |connectorID|
		connector = widget.oscConnectors[connectorID];
		this.view.value_(connectorID);
	}

	prAddController {
		var items, conID;
		var curValue;
		mc.controller ?? {
			mc.controller = SimpleController(mc.model)
		};
		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true);
			mc.controller.put(syncKey, { |changer, what ... moreArgs|
				conID = moreArgs[0];
				all[widget].do { |sel, i|
					items = sel.view.items;
					items[conID] = changer.value[conID];
					curValue = sel.view.value;
					sel.view.items_(items).value_(curValue);
					if (sel.connector === widget.oscConnectors[conID]) {
						sel.view.value_(conID)
					}
				}
			})
		}
	}
}

OscAddrSelect : ConnectorElementView {
	classvar <all;
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
		this.view = PopUpMenu(parentView)
		.items_(widget.midiConnectors.collect(_.name) ++ ["select IP address... (optional)"]);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |sel|

		});
		this.prAddController;
	}

	index_ { |connectorID|
		var display;

		connector = widget.oscConnectors[connectorID];
		mc.model.value[connectorID] !? {
			// TODO: is oscDisplay global to all widgets?
			// this.view.items.indexOf()
		}
	}

	prAddController {

	}
}
