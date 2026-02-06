// MIDI editors

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
		conModel = widget.midiConnectors;

		if (mc.m.value[index].learn == "C") {
			defaultState = [mc.m.value[index].learn, Color.black, Color.green];
			mc.m.value[index].toolTip = "Connect using selected parameters";
		} {
			defaultState = ["L", Color.white, Color.blue];
		};
		this.view = Button(parentView, rect).states_([
			defaultState,
			["X", Color.white, Color.red]
		]).maxWidth_(25).toolTip_(mc.m.value[index].toolTip);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |bt|
			var i = conModel.indexOf(this.connector);
			var src, chan, ctrl;
			mc.m.value[i].learn = bt.states[bt.value][0];
			mc.m.changedPerformKeys(widget.syncKeys, i);
			if (mc.m.value[i].learn == "X") {
				if (mc.m.value[i].src != 'source...') { src = mc.m.value[i].src };
				if (mc.m.value[i].chan != "chan") { chan = mc.m.value[i].chan };
				if (mc.m.value[i].ctrl != "ctrl") { ctrl = mc.m.value[i].ctrl };
				this.connector.midiConnect(src, chan, ctrl);
				if (src.notNil or: { chan.notNil or: { ctrl.notNil }}) {
					all[widget].do { |b|
						if (conModel.indexOf(b.connector) == i) {
							b.view.states_([
								["L", Color.white, Color.blue],
								["X", Color.white, Color.red]
							]).value_(1).toolTip_(mc.m.value[i].toolTip)
						}
					}
				}
			} {
				this.connector.midiDisconnect;
				all[widget].do { |b|
					if (conModel.indexOf(b.connector) == i) {
						b.view.toolTip_(mc.m.value[i].toolTip);
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
		// we need the connector, not its current ID in conModel
		connector = conModel[connectorID];
		mc.m.value[connectorID] !? {
			mc.m.value[connectorID].learn.switch(
				"X", { this.view.value_(1) },
				"L", { this.view.value_(0) }
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
		mc = widget.wmc.midiDisplay;
		conModel = widget.midiConnectors;
		if (mc.m.value[0].learn == "C") {
			defaultState = ["C", Color.black, Color.green];
			mc.m.value[0].toolTip = "Connect using selected parameters";
		} {
			defaultState = ["L", Color.white, Color.blue];
		};
		this.view.states_([
			defaultState,
			["X", Color.white, Color.red]
		]).maxWidth_(25).toolTip_(mc.m.value[0].toolTip);
		// midiConnector at index 0 should always exist (who knows...)
		this.index_(0);
		this.prAddController;
	}

	prAddController {
		var pos, conID;
		mc.c ?? {
			mc.c = SimpleController(mc.m)
		};
		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		// the following is global for all MidiLearnButtons
		// there must be no notion of 'this' as all MidiLearnButton instances are affected
		mc.c.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget].do { |but, i|
				if (but.connector === conModel[conID]) {
					if (changer.value[conID].learn == "C") {
						// mc.m.value[i].toolTip = "Connect using selected parameters";
						but.view.states_([
							["C", Color.black, Color.green],
							["X", Color.white, Color.red]
						])
					};
					pos = but.view.states.detectIndex { |state, j|
						state[0] == changer.value[conID].learn
					};
					defer { but.view.value_(pos).toolTip_(mc.m.value[conID].toolTip) }
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
		conModel = widget.midiConnectors;
		wmc = CVWidget.wmc.midiSources;

		this.view = PopUpMenu(parentView, rect)
		.enabled_(mc.m.value[index].learn != "X")
		.items_(['source...'] ++ wmc.m.value.keys.asArray.sort).maxWidth_(100);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |sel|
			var i = conModel.indexOf(this.connector);
			mc.m.value[i].src = wmc.m.value[sel.item];
			mc.m.value[i].learn = "C";
			mc.m.value[i].toolTip = "Connect using selected parameters";
			mc.m.changedPerformKeys(widget.syncKeys, i);
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

		connector = conModel[connectorID];
		mc.m.value[connectorID] !? {
			display = if (mc.m.value[connectorID].src == 'source...') { 0 } {
				this.view.items.indexOf(
					wmc.m.value.findKeyForValue(mc.m.value[connectorID].src)
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
		conModel = widget.midiConnectors;
		this.view.enabled_(mc.m.value[0].learn != "X")
		.items_(['source...'] ++ wmc.m.value.keys.asArray.sort).maxWidth_(100);
		this.index_(0);
		// midiConnector at index 0 should always exist (who knows...)
		this.prAddController;
	}

	prAddController {
		var conID;
		mc.c ?? {
			mc.c = SimpleController(mc.m)
		};
		wmc.c ?? {
			wmc.c = SimpleController(wmc.m)
		};
		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		CVWidget.syncKeys.indexOf(syncKey) ?? {
			CVWidget.prAddSyncKey(syncKey, true)
		};
		wmc.c.put(syncKey, { |changer, what ... moreArgs|
			all.do { |selects|
				selects.do { |sel|
					defer { sel.view.items_([sel.view.items.first] ++ changer.value.keys.asArray.sort) }
				}
			}
		});
		mc.c.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			changer.value[conID].src;
			all[widget].do { |sel|
				if (sel.connector === conModel[conID]) {
					if (changer.value[conID].src.isNil or: { changer.value[conID].src == 'source...' }) {
						defer {
							sel.view.value_(0);
							sel.view.enabled_(widget.wmc.midiConnections.m.value[conID].isNil);
						}
					} {
						defer {
							sel.view.value_(sel.items.indexOf(
								wmc.m.value.findKeyForValue(changer.value[conID].src)
							));
							sel.view.enabled_(widget.wmc.midiConnections.m.value[conID].isNil);
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
			mc.c.removeAt(syncKey);
			widget.prRemoveSyncKey(syncKey, true);
			wmc.c.removeAt(syncKey);
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
		conModel = widget.midiConnectors;

		this.view = TextField(parentView, rect)
		.enabled_(mc.m.value[index].learn != "X");
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |tf|
			var i = conModel.indexOf(this.connector);
			mc.m.value[i].chan = tf.string;
			mc.m.value[i].learn = "C";
			mc.m.value[i].toolTip = "Connect using selected parameters";
			mc.m.changedPerformKeys(widget.syncKeys, i);
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
		connector = conModel[connectorID];
		mc.m.value[connectorID] !? {
			this.view.string_(mc.m.value[connectorID].chan);
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
		conModel = widget.midiConnectors;
		this.view.enabled_(mc.m.value[0].learn != "X");
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
			all[widget].do { |tf|
				if (tf.connector === conModel[conID]) {
					defer {
						tf.view.string_(changer.value[conID].chan);
						tf.view.enabled_(widget.wmc.midiConnections.m.value[conID].isNil);
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
		conModel = widget.midiConnectors;

		this.view = TextField(parentView, rect)
		.enabled_(mc.m.value[index].learn != "X");
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |tf|
			var i = conModel.indexOf(this.connector);
			mc.m.value[i].ctrl = tf.string;
			mc.m.value[i].learn = "C";
			mc.m.value[i].toolTip = "Connect using selected parameters";
			mc.m.changedPerformKeys(widget.syncKeys, i);
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
		connector = conModel[connectorID];
		mc.m.value[connectorID] !? {
			this.view.string_(mc.m.value[connectorID].ctrl);
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
		conModel = widget.midiConnectors;
		this.view.enabled_(mc.m.value[0].learn != "X");
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
			all[widget].do { |tf|
				if (tf.connector === conModel[conID]) {
					defer {
						tf.view.string_(changer.value[conID].ctrl);
						tf.view.enabled_(widget.wmc.midiConnections.m.value[conID].isNil);
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
		conModel = widget.midiConnectors;

		this.view = PopUpMenu(parentView, rect).items_(["0-127", "endless"]);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |sel|
			var i = conModel.indexOf(this.connector);
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
		connector = conModel[connectorID];
		mc.m.value[connectorID] !? {
			this.view.value_(mc.m.value[connectorID].midiMode)
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
		conModel = widget.midiConnectors;
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
			all[widget].do { |sel|
				if (sel.connector === conModel[conID]) {
					defer { sel.view.value_(changer.value[conID].midiMode) }
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
		conModel = widget.midiConnectors;

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
		connector = conModel[connectorID];
		mc.m.value[connectorID] !? {
			this.view.value_(mc.m.value[connectorID].midiZero)
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
		conModel = widget.midiConnectors;
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
					defer { nb.view.value_(changer.value[conID].midiZero) }
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
		conModel = widget.midiConnectors;

		this.view = NumberBox(parentView, rect).step_(0.1).scroll_step_(0.1).clipLo_(0.0).clipHi_(1.0);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |nb|
			this.connector.setMidiSnapDistance(nb.value);
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
		connector = conModel[connectorID];
		mc.m.value[connectorID] !? {
			this.view.value_(connector.getMidiSnapDistance)
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
		conModel = widget.midiConnectors;
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
		conModel = widget.midiConnectors;

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
		connector = conModel[connectorID];
		mc.m.value[connectorID] !? {
			this.view.value_(mc.m.value[connectorID].midiResolution)
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
		conModel = widget.midiConnectors;
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
		conModel = widget.midiConnectors;

		this.view = NumberBox(parentView, rect).clipLo_(1).step_(1).scroll_step_(1);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |nb|
			this.connector.setMidiCtrlButtonGroup(nb.value.asInteger)
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
		connector = conModel[connectorID];
		mc.m.value[connectorID] !? {
			this.view.value_(mc.m.value[connectorID].ctrlButtonGroup)
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
		conModel = widget.midiConnectors;
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
					defer { nb.view.value_(changer.value[conID].ctrlButtonGroup) }
				}
			}
		})
	}
}

MidiInitButton : ConnectorElementView {
	classvar <all;
	var wmc;


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
			MIDIClient.init;
			try { MIDIIn.connectAll(false) } { |error|
				error.postln;
				"MIDIIn.connectAll failed. Please establish the necessary connections manually.".warn;
			};
			MIDIClient.externalSources.do { |source|
				if (wmc.midiSources.m.value.includes(source.uid).not) {
					wmc.midiSources.m.value.put("% (%)".format(source.name, source.uid).asSymbol, source.uid)
				}
			};
			wmc.midiInitialized.m.value_(MIDIClient.initialized).changedPerformKeys(CVWidget.syncKeys);
			wmc.midiSources.m.changedPerformKeys(CVWidget.syncKeys);
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
		wmc.midiInitialized.c ?? {
			wmc.midiInitialized.c = SimpleController(wmc.midiInitialized.m)
		};
		syncKey = this.class.asSymbol;
		CVWidget.syncKeys.indexOf(syncKey) ?? {
			CVWidget.prAddSyncKey(syncKey, true);
			wmc.midiInitialized.c.put(syncKey, { |changer, what|
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
			wmc.midiInitialized.c.removeAt(syncKey);
			CVWidget.prRemoveSyncKey(syncKey, true);
		}
	}
}
