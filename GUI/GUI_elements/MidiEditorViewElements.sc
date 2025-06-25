// MIDI editors

MidiConnectorNameField : ConnectorElementView {
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

		mc = widget.wmc.midiConnectorNames;
		this.view = TextField(parentView, rect);
		this.index_(index);
		this.view.action_({ |tf|
			this.connector.name_(tf.string.asSymbol)
		});
		this.view.onClose_({ this.close });
		// FIXME: don't let funcs pile up in onRemove
		connectorRemovedFuncAdded ?? {
			MidiConnector.onConnectorRemove_({ |widget, id|
				this.prOnRemoveConnector(widget, id)
			});
			connectorRemovedFuncAdded = true
		};
		this.prAddController;
	}

	index_ { |connectorID|
		connector = widget.midiConnectors[connectorID];
		mc.model.value !? {
			this.view.string_(mc.model.value[connectorID])
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
		mc = widget.wmc.midiConnectorNames;
		// midiConnector at index 0 should always exist (who knows...)
		this.index_(0);
		this.prAddController;
	}

	prAddController {
		var conID;
		mc.controller ?? {
			mc.controller = SimpleController(mc.model)
		};
		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		// if there's already a function defined for synKey simply replace it
		mc.controller.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget].do { |tf|
				if (tf.connector === widget.midiConnectors[conID]) {
					tf.view.string_(changer.value[conID]);
				}
			}
		})
	}
}

MidiConnectorSelect : ConnectorElementView {
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

		mc = widget.wmc.midiConnectorNames;
		this.view = PopUpMenu(parentView)
		.items_(widget.midiConnectors.collect(_.name) ++ ['add MidiConnector...']);
		this.view.onClose_({ this.close });
		this.index_(index);
		connectorRemovedFuncAdded ?? {
			MidiConnector.onConnectorRemove_({ |widget, id|
				this.prOnRemoveConnector(widget, id)
			});
			connectorRemovedFuncAdded = true
		};
		this.prAddController;
	}

	index_ { |connectorID|
		connector = widget.midiConnectors[connectorID];
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
		mc = widget.wmc.midiConnectorNames;
		this.view.items_(widget.midiConnectors.collect(_.name) ++ this.view.items.last);
		// midiConnector at index 0 should always exist (who knows...)
		this.index_(0);
		this.prAddController;
	}

	prAddController {
		var items, conID;
		var curValue;
		mc.controller ?? {
			mc.controller = SimpleController(mc.model)
		};
		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		mc.controller.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget].do { |sel, i|
				items = sel.view.items;
				items[conID] = changer.value[conID];
				curValue = sel.view.value;
				sel.view.items_(items).value_(curValue);
				if (sel.connector === widget.midiConnectors[conID]) {
					sel.view.value_(conID)
				}
			}
		})
	}
}

// Elements must not hold a fixed ID as connectors can get deleted from
// the widget's oscConnectors / midiConnectors lists. Hence, rather determine
// the current index from querying the widget's oscConnectors / midiConnectors list.

MidiLearnButton : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;
	// widget must be a getter as it's called
	// in close(), defined in ConnectorElementView
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
		all[widget] ?? { all.put(widget, List[]) };
		all[widget].add(this);

		mc = widget.wmc.midiDisplay;
		if (mc.model.value[index].learn == "C") {
			defaultState = [mc.model.value[index].learn, Color.black, Color.green];
			mc.model.value[index].toolTip = "Connect using selected parameters";
		} {
			defaultState = ["L", Color.white, Color.blue];
		};
		this.view = Button(parentView, rect).states_([
			defaultState,
			["X", Color.white, Color.red]
		]).maxWidth_(25).toolTip_(mc.model.value[index].toolTip);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |bt|
			var i = widget.midiConnectors.indexOf(this.connector);
			var src, chan, ctrl;
			mc.model.value[i].learn = bt.states[bt.value][0];
			mc.model.changedPerformKeys(widget.syncKeys, i);
			if (mc.model.value[i].learn == "X") {
				if (mc.model.value[i].src != 'source...') { src = mc.model.value[i].src };
				if (mc.model.value[i].chan != "chan") { chan = mc.model.value[i].chan };
				if (mc.model.value[i].ctrl != "ctrl") { ctrl = mc.model.value[i].ctrl };
				widget.midiConnect(connector, src, chan, ctrl);
				if (src.notNil or: { chan.notNil or: { ctrl.notNil }}) {
					all[widget].do { |b|
						if (widget.midiConnectors.indexOf(b.connector) == i) {
							b.view.states_([
								["L", Color.white, Color.blue],
								["X", Color.white, Color.red]
							]).value_(1).toolTip_(mc.model.value[i].toolTip)
						}
					}
				}
			}
			{
				widget.midiDisconnect(connector);
				all[widget].do { |b|
					if (widget.midiConnectors.indexOf(b.connector) == i) {
						b.view.toolTip_(mc.model.value[i].toolTip);
					}
				}
			}
		});
		connectorRemovedFuncAdded ?? {
			MidiConnector.onConnectorRemove_({ |widget, id|
				this.prOnRemoveConnector(widget, id)
			});
			connectorRemovedFuncAdded = true
		};
		this.prAddController;
	}

	// the connector's ID will be dynamic and change
	// any time a connector with a lower ID in the widget's
	// midiConnectors list gets deleted!!!
	index_ { |connectorID|
		// we need the connector, not its current ID in widget.midiConnectors
		// widget.midiConnectors[connectorID] !? {
			connector = widget.midiConnectors[connectorID];
			mc.model.value[connectorID] !? {
				mc.model.value[connectorID].learn.switch(
					"X", {
						this.view.value_(1)
					},
					"L", {
						this.view.value_(0)
					}
				)
			}
	// }
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
		mc = widget.wmc.midiDisplay;
		if (mc.model.value[0].learn == "C") {
			defaultState = [mc.model.value[0].learn, Color.black, Color.green];
			mc.model.value[0].toolTip = "Connect using selected parameters";
		} {
			defaultState = ["L", Color.white, Color.blue];
		};
		this.view.states_([
			defaultState,
			["X", Color.white, Color.red]
		]).maxWidth_(25).toolTip_(mc.model.value[0].toolTip);
		// midiConnector at index 0 should always exist (who knows...)
		this.index_(0);
		this.prAddController;
	}

	prAddController {
		var pos, conID;
		mc.controller ?? {
			mc.controller = SimpleController(mc.model)
		};
		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		// the following is global for all MidiLearnButtons
		// there must be no notion of 'this' as all MidiLearnButton instances are affected
		mc.controller.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget].do { |but, i|
				if (but.connector === widget.midiConnectors[conID]) {
					if (changer.value[conID].learn == "C") {
						// mc.model.value[i].toolTip = "Connect using selected parameters";
						but.view.states_([
							["C", Color.black, Color.green],
							["X", Color.white, Color.red]
						])
					};
					pos = but.view.states.detectIndex { |state, j|
						state[0] == changer.value[conID].learn
					};
					defer { but.view.value_(pos).toolTip_(mc.model.value[conID].toolTip) }
				}
			}
		})
	}

}

MidiSrcSelect : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;
	var <connector, <widget;
	var wmc; // models and controllers tied to the class CVWidget

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, connectorID = 0|
		if (widget.isKindOf(CVWidget).not) {
			Error("arg 'widget' must be a kind of CVWidget").throw
		};
		^super.new.init(parent, widget, rect, connectorID);
	}

	init { |parentView, wdgt, rect, index|
		widget = wdgt;
		all[widget] ?? { all[widget] = List[] };
		all[widget].add(this);

		mc = widget.wmc.midiDisplay;
		wmc = CVWidget.wmc.midiSources;

		this.view = PopUpMenu(parentView, rect)
		.enabled_(mc.model.value[index].learn != "X")
		.items_(['source...'] ++ CVWidget.midiSources.values.sort).maxWidth_(100);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |sel|
			var i = widget.midiConnectors.indexOf(this.connector);
			mc.model.value[i].src = wmc[sel.item];
			mc.model.value[i].learn = "C";
			mc.model.value[i].toolTip = "Connect using selected parameters";
			mc.model.changedPerformKeys(widget.syncKeys, i);
		});
		connectorRemovedFuncAdded ?? {
			MidiConnector.onConnectorRemove_({ |widget, id|
				this.prOnRemoveConnector(widget, id)
			});
			connectorRemovedFuncAdded = true
		};
		this.prAddController;
	}

	// set the view to the specified connector's model value
	index_ { |connectorID|
		var display;

		connector = widget.midiConnectors[connectorID];
		mc.model.value[connectorID] !? {
			display = if (mc.model.value[connectorID].src == 'source...') { 0 } {
				this.view.items.indexOf(
					wmc.model.value.findKeyForValue(mc.model.value[connectorID].src)
				)
			};
			this.view.value_(display)
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
		mc = widget.wmc.midiDisplay;
		this.view.enabled_(mc.model.value[0].learn != "X")
		.items_(['source...'] ++ wmc.model.value.keys.asArray.sort).maxWidth_(100)
		this.index_(0);
		// midiConnector at index 0 should always exist (who knows...)
		this.prAddController;
	}

	prAddController {
		var conID;
		mc.controller ?? {
			mc.controller = SimpleController(mc.model)
		};
		wmc.controller ?? {
			wmc.controller = SimpleController(wmc.model)
		};
		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		CVWidget.syncKeys.indexOf(syncKey) ?? {
			CVWidget.prAddSyncKey(syncKey, true)
		};
		wmc.controller.put(syncKey, { |changer, what ... moreArgs|
			all.do { |selects|
				selects.do { |sel|
					defer { sel.view.items_(['source...'] ++ wmc.model.value.keys.asArray.sort) }
				}
			}
		});
		mc.controller.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			changer.value[conID].src;
			all[widget].do { |sel|
				if (sel.connector === widget.midiConnectors[conID]) {
					if (changer.value[conID].src.isNil or: { changer.value[conID].src == 'source...' }) {
						defer {
							sel.view.value_(0);
							sel.view.enabled_(widget.wmc.midiConnections.model.value[conID].isNil);
						}
					} {
						defer {
							sel.view.value_(sel.items.indexOf(
								wmc.model.value.findKeyForValue(changer.value[conID].src)
							));
							sel.view.enabled_(widget.wmc.midiConnections.model.value[conID].isNil);
						}
					}
				}
			}
		})
	}

	// we need a specially extended version
	// of the cleanup method since we also
	// need to remove the controller from
	// CVWidget.wmc.midiSources and the syncKey
	// from CVWidget.syncKeys
	prCleanup {
		all[widget].remove(this);
		if (all[widget].isEmpty) {
			mc.controller.removeAt(syncKey);
			widget.prRemoveSyncKey(syncKey, true);
			wmc.controller.removeAt(syncKey);
			CVWidget.prRemoveSyncKey(syncKey, true);
		}
	}

}

MidiChanField : ConnectorElementView {
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

		mc = widget.wmc.midiDisplay;
		this.view = TextField(parentView, rect)
		.enabled_(mc.model.value[index].learn != "X");
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |tf|
			var i = widget.midiConnectors.indexOf(this.connector);
			mc.model.value[i].chan = tf.string;
			mc.model.value[i].learn = "C";
			mc.model.value[i].toolTip = "Connect using selected parameters";
			mc.model.changedPerformKeys(widget.syncKeys, i);
		});
		connectorRemovedFuncAdded ?? {
			MidiConnector.onConnectorRemove_({ |widget, id|
				this.prOnRemoveConnector(widget, id)
			});
			connectorRemovedFuncAdded = true
		};
		this.prAddController;
	}

	// set the view to the specified connector's model value
	index_ { |connectorID|
		connector = widget.midiConnectors[connectorID];
		mc.model.value[connectorID] !? {
			this.view.string_(mc.model.value[connectorID].chan);
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
		mc = widget.wmc.midiDisplay;
		this.view.enabled_(mc.model.value[0].learn != "X");
		this.index_(0);
		// midiConnector at index 0 should always exist (who knows...)
		this.prAddController;
	}

	prAddController {
		var conID;
		mc.controller ?? {
			mc.controller = SimpleController(mc.model)
		};
		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		mc.controller.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget].do { |tf|
				if (tf.connector === widget.midiConnectors[conID]) {
					defer {
						tf.view.string_(changer.value[conID].chan);
						tf.view.enabled_(widget.wmc.midiConnections.model.value[conID].isNil);
					}
				}
			}
		})
	}
}

MidiCtrlField : ConnectorElementView {
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

		mc = widget.wmc.midiDisplay;
		this.view = TextField(parentView, rect)
		.enabled_(mc.model.value[index].learn != "X");
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |tf|
			var i = widget.midiConnectors.indexOf(this.connector);
			mc.model.value[i].ctrl = tf.string;
			mc.model.value[i].learn = "C";
			mc.model.value[i].toolTip = "Connect using selected parameters";
			mc.model.changedPerformKeys(widget.syncKeys, i);
		});
		connectorRemovedFuncAdded ?? {
			MidiConnector.onConnectorRemove_({ |widget, id|
				this.prOnRemoveConnector(widget, id)
			});
			connectorRemovedFuncAdded = true
		};
		this.prAddController;
	}

	// set the view to the specified connector's model value
	index_ { |connectorID|
		connector = widget.midiConnectors[connectorID];
		mc.model.value[connectorID] !? {
			this.view.string_(mc.model.value[connectorID].ctrl);
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
		mc = widget.wmc.midiDisplay;
		this.view.enabled_(mc.model.value[0].learn != "X");
		this.index_(0);
		// midiConnector at index 0 should always exist (who knows...)
		this.prAddController;
	}

	prAddController {
		var conID;
		mc.controller ?? {
			mc.controller = SimpleController(mc.model)
		};
		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		mc.controller.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget].do { |tf|
				if (tf.connector === widget.midiConnectors[conID]) {
					defer {
						tf.view.string_(changer.value[conID].ctrl);
						tf.view.enabled_(widget.wmc.midiConnections.model.value[conID].isNil);
					}
				}
			}
		})
	}
}

MidiModeSelect : ConnectorElementView {
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
		all[widget] ?? { all[wdgt] = List[] };
		all[widget].add(this);

		mc = widget.wmc.midiOptions;
		this.view = PopUpMenu(parentView, rect).items_(["0-127", "endless"]);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |sel|
			var i = widget.midiConnectors.indexOf(this.connector);
			this.connector.setMidiMode(sel.value);
		});
		connectorRemovedFuncAdded ?? {
			MidiConnector.onConnectorRemove_({ |widget, id|
				this.prOnRemoveConnector(widget, id)
			});
			connectorRemovedFuncAdded = true
		};
		this.prAddController;
	}

	// index_ the view to the specified connector's model value
	index_ { |connectorID|
		connector = widget.midiConnectors[connectorID];
		mc.model.value[connectorID] !? {
			this.view.value_(mc.model.value[connectorID].midiMode)
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
		mc = widget.wmc.midiOptions;
		this.index_(0);
		// midiConnector at index 0 should always exist (who knows...)
		this.prAddController;
	}

	prAddController {
		var conID;
		mc.controller ?? {
			mc.controller = SimpleController(mc.model)
		};
		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		mc.controller.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget].do { |sel|
				if (sel.connector === widget.midiConnectors[conID]) {
					defer { sel.view.value_(changer[conID].value.midiMode) }
				}
			}
		})
	}
}

MidiZeroNumberBox : ConnectorElementView {
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

		mc = widget.wmc.midiOptions;
		this.view = NumberBox(parentView, rect);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |nb|
			this.connector.setMidiZero(nb.value);
		});
		connectorRemovedFuncAdded ?? {
			MidiConnector.onConnectorRemove_({ |widget, id|
				this.prOnRemoveConnector(widget, id)
			});
			connectorRemovedFuncAdded = true
		};
		this.prAddController;
	}

	// set the view to the specified connector's model value
	index_ { |connectorID|
		connector = widget.midiConnectors[connectorID];
		mc.model.value[connectorID] !? {
			this.view.value_(mc.model.value[connectorID].midiZero)
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
		mc = widget.wmc.midiOptions;
		this.index_(0);
		// midiConnector at index 0 should always exist (who knows...)
		this.prAddController;
	}

	prAddController {
		var conID;
		mc.controller ?? {
			mc.controller = SimpleController(mc.model)
		};
		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		mc.controller.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget].do { |nb|
				if (nb.connector === widget.midiConnectors[conID]) {
					defer { nb.view.value_(changer[conID].value.midiZero) }
				}
			}
		})
	}
}

SnapDistanceNumberBox : ConnectorElementView {
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

		mc = widget.wmc.midiOptions;
		this.view = NumberBox(parentView, rect).step_(0.1).scroll_step_(0.1).clipLo_(0.0).clipHi_(1.0);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |nb|
			this.connector.setSnapDistance(nb.value);
		});
		connectorRemovedFuncAdded ?? {
			MidiConnector.onConnectorRemove_({ |widget, id|
				this.prOnRemoveConnector(widget, id)
			});
			connectorRemovedFuncAdded = true
		};
		this.prAddController;
	}

	// set the view to the specified connector's model value
	index_ { |connectorID|
		connector = widget.midiConnectors[connectorID];
		mc.model.value[connectorID] !? {
			this.view.value_(mc.model.value[connectorID].snapDistance)
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
		mc = widget.wmc.midiOptions;
		this.index_(0);
		// midiConnector at index 0 should always exist (who knows...)
		this.prAddController;
	}

	prAddController {
		var conID;
		mc.controller ?? {
			mc.controller = SimpleController(mc.model)
		};
		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		mc.controller.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget].do { |nb|
				if (nb.connector === widget.midiConnectors[conID]) {
					defer { nb.view.value_(changer.value[conID].snapDistance) }
				}
			}
		})
	}
}

MidiResolutionNumberBox : ConnectorElementView {
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

		mc = widget.wmc.midiOptions;
		this.view = NumberBox(parentView, rect).clipLo_(0).scroll_step_(0.1).step_(0.1);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |nb|
			this.connector.setMidiResolution(nb.value);
		});
		connectorRemovedFuncAdded ?? {
			MidiConnector.onConnectorRemove_({ |widget, id|
				this.prOnRemoveConnector(widget, id)
			});
			connectorRemovedFuncAdded = true
		};
		this.prAddController;
	}

	// set the view to the specified connector's model value
	index_ { |connectorID|
		connector = widget.midiConnectors[connectorID];
		mc.model.value[connectorID] !? {
			this.view.value_(mc.model.value[connectorID].midiResolution)
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
		mc = widget.wmc.midiOptions;
		this.index_(0);
		// midiConnector at index 0 should always exist (who knows...)
		this.prAddController;
	}

	prAddController {
		var conID;
		mc.controller ?? {
			mc.controller = SimpleController(mc.model)
		};
		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		mc.controller.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget].do { |nb|
				if (nb.connector === widget.midiConnectors[conID]) {
					defer { nb.view.value_(changer.value[conID].midiResolution) }
				}
			}
		})
	}
}

SlidersPerGroupNumberBox : ConnectorElementView {
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

		mc = widget.wmc.midiOptions;
		this.view = NumberBox(parentView, rect).clipLo_(1).step_(1).scroll_step_(1);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |nb|
			this.connector.setCtrlButtonGroup(nb.value.asInteger)
		});
		connectorRemovedFuncAdded ?? {
			MidiConnector.onConnectorRemove_({ |widget, id|
				this.prOnRemoveConnector(widget, id)
			});
			connectorRemovedFuncAdded = true
		};
		this.prAddController;
	}

	// set the view to the specified connector's model value
	index_ { |connectorID|
		connector = widget.midiConnectors[connectorID];
		mc.model.value[connectorID] !? {
			this.view.value_(mc.model.value[connectorID].ctrlButtonGroup)
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
		mc = widget.wmc.midiOptions;
		this.index_(0);
		// midiConnector at index 0 should always exist (who knows...)
		this.prAddController;
	}

	prAddController {
		var conID;
		mc.controller ?? {
			mc.controller = SimpleController(mc.model)
		};
		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		mc.controller.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget].do { |nb|
				if (nb.connector === widget.midiConnectors[conID]) {
					defer { nb.view.value_(changer.value[conID].ctrlButtonGroup) }
				}
			}
		})
	}
}

MidiInitButton : ConnectorElementView {
	classvar <all;
	var syncKey, wmc;


	*initClass {
		all = List[];
	}

	*new { |parent, rect|
		^super.new.init(parent, rect)
	}

	init { |parentView, rect|
		var midiConnectAll = {
			try { MIDIIn.connectAll } { |error|
				error.postln;
				"MIDIIn.connectAll failed. Please establish the necessary connections manually".warn;
			}
		};

		all.add(this);
		wmc = CVWidget.wmc;

		this.view = Button(parentView, rect)
		.action_({ |bt|
			try { MIDIIn.connectAll } { |error|
				error.postln;
				"MIDIIn.connectAll failed. Please establish the necessary connections manually.".warn;
			};
			MIDIClient.externalSources.do { |source|
				if (wmc.midiSources.model.value.includes(source.uid).not) {
					wmc.midiSources.model.value.put("% (%)".format(source.name, source.uid).asSymbol, source.uid)
				}
			};
			wmc.midiInitialized.model.value_(MIDIClient.initialized).changedPerformKeys(CVWidget.syncKeys);
			wmc.midiSources.model.changedPerformKeys(CVWidget.syncKeys);
		});
		this.view.onClose_({ this.close });


		if (MIDIClient.initialized) {
			this.view.states_([["reinit MIDI", Color.white, Color.red]]);
		} {
			this.view.states_([["init MIDI", Color.black, Color.green]]);
		};
		this.prAddController;
	}

	index_ {}

	widget_ {}

	prAddController {
		wmc.midiInitialized.controller ?? {
			wmc.midiInitialized.controller = SimpleController(wmc.midiInitialized.model)
		};
		syncKey = this.class.asSymbol;
		CVWidget.syncKeys.indexOf(syncKey) ?? {
			CVWidget.prAddSyncKey(syncKey, true);
			wmc.midiInitialized.controller.put(syncKey, { |changer, what|
				all.do { |bt|
					if (changer.value) {
						bt.view.states_([["reinit MIDI", Color.white, Color.red]]);
					} {
						bt.view.states_([["init MIDI", Color.black, Color.green]]);
					}
				}
			})
		}
	}

	close {
		this.remove;
		this.viewDidClose;
		this.prCleanup;
	}

	prCleanup {
		all.remove(this);
		if (all.isEmpty) {
			wmc.midiInitialized.controller.removeAt(syncKey);
			CVWidget.prRemoveSyncKey(syncKey, true);
		}
	}

	// prOnRemoveConnector {}
}

MidiConnectorRemoveButton : ConnectorElementView {
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

		this.index_(index);
		this.view = Button(parentView, rect)
		.states_([["remove Connector", Color.white, Color(0, 0.5, 0.5)]])
		.action_({ this.connector.remove });
		connectorRemovedFuncAdded ?? {
			MidiConnector.onConnectorRemove_({ |widget, id|
				this.prOnRemoveConnector(widget, id)
			});
			connectorRemovedFuncAdded = true
		};
	}

	index_ { |connectorID|
		connector = widget.midiConnectors[connectorID];
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
		this.index_(0);
	}

	close {
		this.remove;
		this.viewDidClose;
		this.prCleanup;
	}

	prCleanup {
		all[widget].remove(this);
	}
}
