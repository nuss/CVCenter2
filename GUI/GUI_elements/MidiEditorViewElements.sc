// MIDI editors

// Elements must not hold a fixed ID as connectors can get deleted from
// the widget's oscConnectors / midiConnectors lists. Hence, rather determine
// the current index from querying the widget's oscConnectors / midiConnectors list.

MidiLearnButton : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, slot, connectorID(0)|
		if (widget.isKindOf(CVWidget).not) {
			Error("arg 'widget' must be a kind of CVWidget").throw
		};
		^super.newCopyArgs(widget: widget, slot: slot).init(parent, rect, connectorID);
	}

	init { |parentView, rect, index|
		var defaultState;
		var src, chan, ctrl, argTemplate, dispatcher;
		var conID;

		all[widget] ?? { all.put(widget, List[]) };
		all[widget].add(this);

		this.prMCDistinct(\midi);

		if (displayM.value[index].learn == "C") {
			defaultState = [displayM.value[index].learn, Color.black, Color.green];
			displayM.value[index].toolTip = "Connect using selected parameters";
		} {
			defaultState = ["L", Color.white, Color.blue];
		};
		this.view = Button(parentView, rect).states_([
			defaultState,
			["X", Color.white, Color.red]
		]).maxWidth_(25).toolTip_(displayM.value[index].toolTip);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |bt|
			conID = connector.index;
			displayM.value[conID].learn = bt.states[bt.value][0];
			displayM.changedPerformKeys(widget.syncKeys, conID);
			if (displayM.value[conID].learn == "X") {
				if (displayM.value[conID].src != 'source...') { src = displayM.value[conID].src };
				if (displayM.value[conID].chan != "chan") { chan = displayM.value[conID].chan };
				if (displayM.value[conID].ctrl != "ctrl") { ctrl = displayM.value[conID].ctrl };
				argTemplate = this.connector.getMidiTemplate;
				dispatcher = this.connector.getMidiDispatcher;
				this.connector.midiConnect(src, chan, ctrl, argTemplate, dispatcher);
				if (src.notNil or: { chan.notNil or: { ctrl.notNil }}) {
					all[widget].do { |b|
						if (connectors.indexOf(b.connector) == conID) {
							b.view.states_([
								["L", Color.white, Color.blue],
								["X", Color.white, Color.red]
							]).value_(1).toolTip_(displayM.value[conID].toolTip)
						}
					}
				}
			} {
				this.connector.midiDisconnect;
				all[widget].do { |b|
					if (connectors.indexOf(b.connector) == conID) {
						b.view.toolTip_(displayM.value[conID].toolTip);
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
		// we need the connector, not its current ID in connectors
		connector = connectors[connectorID];
		displayM.value[connectorID] !? {
			displayM.value[connectorID].learn.switch(
				"X", { this.view.value_(1) },
				"L", { this.view.value_(0) }
			)
		}
	}

	widget_ { |otherWidget, slot|
		var defaultState;

		all[otherWidget] ?? { all[otherWidget] = List[] };
		all[otherWidget].add(this);
		this.prCleanup;
		// switch after cleanup has finished
		widget = otherWidget;
		this.slot_(slot);
		this.prMCDistinct(\midi);
		if (displayM.value[0].learn == "C") {
			defaultState = ["C", Color.black, Color.green];
			displayM.value[0].toolTip = "Connect using selected parameters";
		} {
			defaultState = ["L", Color.white, Color.blue];
		};
		this.view.states_([
			defaultState,
			["X", Color.white, Color.red]
		]).maxWidth_(25).toolTip_(displayM.value[0].toolTip);
		// midiConnector at index 0 should always exist (who knows...)
		this.index_(0);
		this.prAddController;
	}

	prAddController {
		var pos, conID;

		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		// the following is global for all MidiLearnButtons
		// there must be no notion of 'this' as all MidiLearnButton instances are affected
		displayC.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget].do { |but, i|
				if (but.connector === connectors[conID]) {
					if (changer.value[conID].learn == "C") {
						// displayM.value[conID].toolTip = "Connect using selected parameters";
						but.view.states_([
							["C", Color.black, Color.green],
							["X", Color.white, Color.red]
						])
					};
					pos = but.view.states.detectIndex { |state, j|
						state[0] == changer.value[conID].learn
					};
					defer { but.view.value_(pos).toolTip_(displayM.value[conID].toolTip) }
				}
			}
		})
	}

}

MidiSrcSelect : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;
	var wmc; // models and controllers tied to the class CVWidget

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, slot, connectorID(0)|
		if (widget.isKindOf(CVWidget).not) {
			Error("arg 'widget' must be a kind of CVWidget").throw
		};
		^super.newCopyArgs(widget: widget, slot: slot).init(parent, rect, connectorID);
	}

	init { |parentView, rect, index|
		var conID;

		all[widget] ?? { all[widget] = List[] };
		all[widget].add(this);

		// mc = widget.wmc.midiDisplay;
		// connectors = widget.midiConnectors;
		this.prMCDistinct(\midi);
		wmc = CVWidget.wmc.midiSources;

		this.view = PopUpMenu(parentView, rect)
		.enabled_(displayM.value[index].learn != "X")
		.items_(['source...'] ++ wmc.m.value.keys.asArray.sort).maxWidth_(100);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |sel|
			conID = this.connector.index;
			displayM.value[conID].src = wmc.m.value[sel.item];
			displayM.value[conID].learn = "C";
			displayM.value[conID].toolTip = "Connect using selected parameters";
			displayM.changedPerformKeys(widget.syncKeys, conID);
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

		connector = connectors[connectorID];
		displayM.value[connectorID] !? {
			display = if (displayM.value[connectorID].src == 'source...') { 0 } {
				this.view.items.indexOf(
					wmc.m.value.findKeyForValue(displayM.value[connectorID].src)
				)
			};
			this.view.value_(display)
		}
	}

	widget_ { |otherWidget, slot|
		all[otherWidget] ?? { all[otherWidget] = List[] };
		all[otherWidget].add(this);
		this.prCleanup;
		// switch after cleanup has finished
		widget = otherWidget;
		this.slot_(slot);
		this.prMCDistinct(\midi);
		this.view.enabled_(displayM.value[0].learn != "X")
		.items_(['source...'] ++ wmc.m.value.keys.asArray.sort).maxWidth_(100);
		this.index_(0);
		// midiConnector at index 0 should always exist (who knows...)
		this.prAddController;
	}

	prAddController {
		var conID;

		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		CVWidget.syncKeys.indexOf(syncKey) ?? {
			CVWidget.prAddSyncKey(syncKey, true)
		};
		wmc.c ?? { wmc.c = SimpleController(wmc.m) };
		wmc.c.put(syncKey, { |changer, what ... moreArgs|
			all.do { |selects|
				selects.do { |sel|
					defer { sel.view.items_([sel.view.items.first] ++ changer.value.keys.asArray.sort) }
				}
			}
		});
		displayC.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			changer.value[conID].src;
			all[widget].do { |sel|
				if (sel.connector === connectors[conID]) {
					if (changer.value[conID].src.isNil or: { changer.value[conID].src == 'source...' }) {
						defer {
							sel.view.value_(0);
							sel.view.enabled_(connectionsM.value[conID].isNil);
						}
					} {
						defer {
							sel.view.value_(sel.items.indexOf(
								wmc.m.value.findKeyForValue(changer.value[conID].src)
							));
							sel.view.enabled_(connectionsM.value[conID].isNil);
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
			this.prRemoveControllers;
			widget.prRemoveSyncKey(syncKey, true);
			wmc.c.removeAt(syncKey);
			CVWidget.prRemoveSyncKey(syncKey, true);
		}
	}

}

MidiChanField : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, slot, connectorID(0)|
		if (widget.isKindOf(CVWidget).not) {
			Error("arg 'widget' must be a kind of CVWidget").throw
		};
		^super.newCopyArgs(widget: widget, slot: slot).init(parent, rect, connectorID);
	}

	init { |parentView, rect, index|
		var conID;

		all[widget] ?? { all[widget] = List[] };
		all[widget].add(this);

		// mc = widget.wmc.midiDisplay;
		// connectors = widget.midiConnectors;
		this.prMCDistinct(\midi);
		this.view = TextField(parentView, rect)
		.enabled_(displayM.value[index].learn != "X");
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |tf|
			conID = connector.index;
			displayM.value[conID].chan = tf.string;
			displayM.value[conID].learn = "C";
			displayM.value[conID].toolTip = "Connect using selected parameters";
			displayM.changedPerformKeys(widget.syncKeys, conID);
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
		connector = connectors[connectorID];
		displayM.value[connectorID] !? {
			this.view.string_(displayM.value[connectorID].chan);
		}
	}

	widget_ { |otherWidget, slot|
		all[otherWidget] ?? { all[otherWidget] = List[] };
		all[otherWidget].add(this);
		this.prCleanup;
		// switch after cleanup has finished
		widget = otherWidget;
		this.slot_(slot);
		this.prMCDistinct(\midi);
		this.view.enabled_(displayM.value[0].learn != "X");
		this.index_(0);
		// midiConnector at index 0 should always exist (who knows...)
		this.prAddController;
	}

	prAddController {
		var conID;

		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		displayC.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget].do { |tf|
				if (tf.connector === connector[conID]) {
					defer {
						tf.view.string_(changer.value[conID].chan);
						tf.view.enabled_(connectionsM.value[conID].isNil);
					}
				}
			}
		})
	}
}

MidiCtrlField : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, slot, connectorID(0)|
		if (widget.isKindOf(CVWidget).not) {
			Error("arg 'widget' must be a kind of CVWidget").throw
		};
		^super.newCopyArgs(widget: widget, slot: slot).init(parent, rect, connectorID);
	}

	init { |parentView, rect, index|
		var conID;

		all[widget] ?? { all[widget] = List[] };
		all[widget].add(this);

		this.prMCDistinct(\midi);
		this.view = TextField(parentView, rect)
		.enabled_(displayM.value[index].learn != "X");
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |tf|
			conID = connector.index;
			displayM.value[conID].ctrl = tf.string;
			displayM.value[conID].learn = "C";
			displayM.value[conID].toolTip = "Connect using selected parameters";
			displayM.changedPerformKeys(widget.syncKeys, conID);
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
		connector = connectors[connectorID];
		displayM.value[connectorID] !? {
			this.view.string_(displayM.value[connectorID].ctrl);
		}
	}

	widget_ { |otherWidget, slot|
		all[otherWidget] ?? { all[otherWidget] = List[] };
		all[otherWidget].add(this);
		this.prCleanup;
		// switch after cleanup has finished
		widget = otherWidget;
		this.slot_(slot);
		this.prMCDistinct(\midi);
		this.view.enabled_(displayM.value[0].learn != "X");
		this.index_(0);
		// midiConnector at index 0 should always exist (who knows...)
		this.prAddController;
	}

	prAddController {
		var conID;

		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		displayC.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget].do { |tf|
				if (tf.connector === connectors[conID]) {
					defer {
						tf.view.string_(changer.value[conID].ctrl);
						tf.view.enabled_(connectionsM.value[conID].isNil);
					}
				}
			}
		})
	}
}

MidiModeSelect : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, slot, connectorID(0)|
		if (widget.isKindOf(CVWidget).not) {
			Error("arg 'widget' must be a kind of CVWidget").throw
		};
		^super.newCopyArgs(widget: widget, slot: slot).init(parent, rect, connectorID);
	}

	init { |parentView, rect, index|
		all[widget] ?? { all[widget] = List[] };
		all[widget].add(this);

		this.prMCDistinct(\midi);
		this.view = PopUpMenu(parentView, rect).items_(["0-127", "endless"]);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.action_({ |sel|
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
		connector = connectors[connectorID];
		optionsM.value[connectorID] !? {
			this.view.value_(optionsM.value[connectorID].midiMode)
		}
	}

	widget_ { |otherWidget, slot|
		all[otherWidget] ?? { all[otherWidget] = List[] };
		all[otherWidget].add(this);
		this.prCleanup;
		// switch after cleanup has finished
		widget = otherWidget;
		this.slot_(slot);
		this.prMCDistinct(\midi);
		this.index_(0);
		// midiConnector at index 0 should always exist (who knows...)
		this.prAddController;
	}

	prAddController {
		var conID;

		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		optionsC.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget].do { |sel|
				if (sel.connector === connectors[conID]) {
					defer { sel.view.value_(changer.value[conID].midiMode) }
				}
			}
		})
	}
}

MidiZeroNumberBox : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, slot, connectorID(0)|
		if (widget.isKindOf(CVWidget).not) {
			Error("arg 'widget' must be a kind of CVWidget").throw
		};
		^super.newCopyArgs(widget: widget, slot: slot).init(parent, rect, connectorID);
	}

	init { |parentView, rect, index|
		all[widget] ?? { all[widget] = List[] };
		all[widget].add(this);

		this.prMCDistinct(\midi);
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
		connector = connectors[connectorID];
		optionsM.value[connectorID] !? {
			this.view.value_(optionsM.value[connectorID].midiZero)
		}
	}

	widget_ { |otherWidget, slot|
		all[otherWidget] ?? { all[otherWidget] = List[] };
		all[otherWidget].add(this);
		this.prCleanup;
		// switch after cleanup has finished
		widget = otherWidget;
		this.slot_(slot);
		this.prMCDistinct(\midi);
		this.index_(0);
		// midiConnector at index 0 should always exist (who knows...)
		this.prAddController;
	}

	prAddController {
		var conID;

		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		optionsC.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget].do { |nb|
				if (nb.connector === connectors[conID]) {
					defer { nb.view.value_(changer.value[conID].midiZero) }
				}
			}
		})
	}
}

SnapDistanceNumberBox : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, slot, connectorID(0)|
		if (widget.isKindOf(CVWidget).not) {
			Error("arg 'widget' must be a kind of CVWidget").throw
		};
		^super.newCopyArgs(widget: widget, slot: slot).init(parent, rect, connectorID);
	}

	init { |parentView, rect, index|
		all[widget] ?? { all[widget] = List[] };
		all[widget].add(this);

		// mc = widget.wmc.midiOptions;
		// connectors = widget.midiConnectors;
		this.prMCDistinct(\midi);
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
		connector = connectors[connectorID];
		optionsM.value[connectorID] !? {
			this.view.value_(connector.getMidiSnapDistance)
		}
	}

	widget_ { |otherWidget, slot|
		all[otherWidget] ?? { all[otherWidget] = List[] };
		all[otherWidget].add(this);
		this.prCleanup;
		// switch after cleanup has finished
		widget = otherWidget;
		this.slot_(slot);
		this.prMCDistinct(\midi);
		this.index_(0);
		// midiConnector at index 0 should always exist (who knows...)
		this.prAddController;
	}

	prAddController {
		var conID;

		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		optionsC.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget].do { |nb|
				if (nb.connector === connectors[conID]) {
					defer { nb.view.value_(changer.value[conID].snapDistance) }
				}
			}
		})
	}
}

MidiResolutionNumberBox : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, slot, connectorID(0)|
		if (widget.isKindOf(CVWidget).not) {
			Error("arg 'widget' must be a kind of CVWidget").throw
		};
		^super.newCopyArgs(widget: widget, slot: slot).init(parent, rect, connectorID);
	}

	init { |parentView, rect, index|
		all[widget] ?? { all[widget] = List[] };
		all[widget].add(this);

		this.prMCDistinct(\midi);
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
		connector = connectors[connectorID];
		optionsM.value[connectorID] !? {
			this.view.value_(optionsM.value[connectorID].midiResolution)
		}
	}

	widget_ { |otherWidget, slot|
		all[otherWidget] ?? { all[otherWidget] = List[] };
		all[otherWidget].add(this);
		this.prCleanup;
		// switch after cleanup has finished
		widget = otherWidget;
		this.slot_(slot);
		this.prMCDistinct(\midi);
		this.index_(0);
		// midiConnector at index 0 should always exist (who knows...)
		this.prAddController;
	}

	prAddController {
		var conID;

		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		optionsC.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget].do { |nb|
				if (nb.connector === connectors[conID]) {
					defer { nb.view.value_(changer.value[conID].midiResolution) }
				}
			}
		})
	}
}

SlidersPerGroupNumberBox : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, slot, connectorID(0)|
		if (widget.isKindOf(CVWidget).not) {
			Error("arg 'widget' must be a kind of CVWidget").throw
		};
		^super.newCopyArgs(widget: widget, slot: slot).init(parent, rect, connectorID);
	}

	init { |parentView, rect, index|
		all[widget] ?? { all[widget] = List[] };
		all[widget].add(this);

		// mc = widget.wmc.midiOptions;
		// connectors = widget.midiConnectors;
		this.prMCDistinct(\midi);
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
		connector = connectors[connectorID];
		optionsM.value[connectorID] !? {
			this.view.value_(optionsM.value[connectorID].ctrlButtonGroup)
		}
	}

	widget_ { |otherWidget, slot|
		all[otherWidget] ?? { all[otherWidget] = List[] };
		all[otherWidget].add(this);
		this.prCleanup;
		// switch after cleanup has finished
		widget = otherWidget;
		this.slot_(slot);
		this.prMCDistinct(\midi);
		this.index_(0);
		// midiConnector at index 0 should always exist (who knows...)
		this.prAddController;
	}

	prAddController {
		var conID;

		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		optionsC.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget].do { |nb|
				if (nb.connector === connectors[conID]) {
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
