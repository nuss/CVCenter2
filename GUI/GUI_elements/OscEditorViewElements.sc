// OSC Editors

OscCmdNameField : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;
	var connections;
	var validOsc = "^/[\\w\\d\\H/]+[\\w\\d\\H]+[^/\\h]$";

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
		var action, conID;

		all[widget] ?? { all[widget] = List[] };
		all[widget].add(this);

		this.prMCDistinct(\osc);
		this.view = TextField(parentView, rect);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.enabled_(connectionsM.value[index].isNil);
		action = { |tf|
			conID = connector.index;
			this.connector.setOscCmdName(tf.string);
			if (tf.string.asSymbol !== '/path/to/cmd') {
				if (tf.string.size > 0) {
					displayM.value[conID].learn = false;
					if (validOsc.matchRegexp(tf.string)) {
						// "textfield string matching".postln;
						displayM.value[conID].connectState = ["connect", Color.white, Color.blue];
						displayM.value[conID].connectEnabled = true;
						displayM.value[conID].connectWarning = nil;
					} {
						// "textfield string not matching".postln;
						displayM.value[conID].connectState = ["connect", Color.white, Color.gray];
						displayM.value[conID].connectEnabled = false;
						displayM.value[conID].connectWarning = "The given OSC message is invalid: OSC messages must begin with a slash and must not contain spaces."
					}
				} {
					displayM.value[conID].learn = true;
					displayM.value[conID].connectState = ["learn", Color.yellow, Color.green(0.5)];
					displayM.value[conID].connectEnabled = true;
					displayM.value[conID].connectWarning = nil;
				};
				displayM.changedPerformKeys(widget.syncKeys, conID);
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
		connector = connectors[connectorID];
		// is this right?
		displayM.value[connectorID] !? {
			this.view.string_(displayM.value[connectorID].nameField)
		};
		this.view.enabled_(connectionsM.value[connectorID].isNil)
	}

	setWidget { |otherWidget, slot|
		// FIXME: check for CVWidget2D slot (once it's implemented...)
		if (otherWidget.class !== CVWidgetKnob) {
			Error("Widget must be a CVWidgetKnob").throw
		};

		all[otherWidget] ?? { all[otherWidget] = List[] };
		all[otherWidget].add(this);

		this.prCleanup;
		// switch after cleanup has finished
		widget = otherWidget;
		this.prMCDistinct(\osc);
		this.slot_(slot);
		this.view.enabled_(connectionsM.value[0].isNil);
		this.index_(0);
		// oscConnector at index 0 should always exist (who knows...)
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
					// displayM.value[conID].learn = "connect";
					defer {
						tf.view.string_(changer.value[conID].nameField);
						tf.view.toolTip_(changer.value[conID].connectWarning);
					}
				}
			}
		});
		connectionsC.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget].do { |tf|
				if (tf.connector === connectors[conID]) {
					defer { tf.view.enabled_(changer.value[conID].isNil) }
				}
			}
		})
	}
}

// TODO: rename to OscMasgIndexBox
OscMsgIndexBox : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;
	var connections;

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

		this.prMCDistinct(\osc);
		this.view = NumberBox(parentView, rect)
		.clipLo_(1).step_(1).scroll_step_(1)
		.toolTip_("If OSC message conatains more than one value select message slot that shall be read");
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.enabled_(connectionsM.value[index].isNil);
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
		connector = connectors[connectorID];
		displayM.value[connectorID] !? {
			this.view.value_(displayM.value[connectorID].index)
		};
		this.view.enabled_(connectionsM.value[connectorID].isNil);
	}

	setWidget { |otherWidget, slot|
		if (otherWidget.class !== CVWidgetKnob) {
			Error("Widget must be a CVWidgetKnob").throw
		};

		all[otherWidget] ?? { all[otherWidget] = List[] };
		all[otherWidget].add(this);
		this.prCleanup;
		// switch after cleanup has finished
		widget = otherWidget;
		this.slot_(slot);
		this.prMCDistinct(\osc);
		this.view.enabled_(connectionsM.value[0].isNil);
		this.index_(0);
		// oscConnector at index 0 should always exist (who knows...)
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
			all[widget].do { |nb|
				if (nb.connector === connectors[conID]) {
					defer {
						nb.view.clipHi_(changer.value[conID].numMsgSlots)
						.value_(changer.value[conID].index)
					}
				}
			}
		});
		connectionsC.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget].do { |nb|
				if (nb.connector === connectors[conID]) {
					defer { nb.view.enabled_(changer.value[conID].isNil )}
				}
			}
		})
	}
}

OscModeSelect : ConnectorElementView {
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

		this.prMCDistinct(\osc);
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
		connector = connectors[connectorID];
		optionsM.value[connectorID] !? {
			this.view.value_(optionsM.value[connectorID].oscEndless)
		}
	}


	setWidget { |otherWidget, slot|
		all[otherWidget] ?? { all[otherWidget] = List[] };
		all[otherWidget].add(this);
		this.prCleanup;
		// switch after cleanup has finished
		widget = otherWidget;
		this.slot_(slot);
		this.prMCDistinct(\osc);
		this.index_(0);
		// oscConnector at index 0 should always exist (who knows...)
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
					defer { nb.view.value_(changer.value[conID].oscEndless) }
				}
			}
		})
	}
}

OscMatchingCheckBox : ConnectorElementView {
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

		this.prMCDistinct(\osc);
		this.view = CheckBox(parentView, rect)
		.toolTip_("Create \"matching\" OSCFunc");
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.enabled_(connectionsM.value[index].isNil);
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
		connector = connectors[connectorID];
		optionsM.value[connectorID] !? {
			this.view.value_(connector.getOscMatching)
		};
		this.view.enabled_(connectionsM.value[connectorID].isNil);
	}

	setWidget { |otherWidget, slot|
		all[otherWidget] ?? { all[otherWidget] = List[] };
		all[otherWidget].add(this);
		this.prCleanup;
		// switch after cleanup has finished
		widget = otherWidget;
		this.slot_(slot);
		this.prMCDistinct(\osc);
		this.view.enabled_(connectionsM.value[0].isNil);
		this.index_(0);
		// oscConnector at index 0 should always exist (who knows...)
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
					defer { nb.view.value_(changer.value[conID].oscMatching) }
				}
			}
		});
		connectionsC.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget].do { |cb|
				if (cb.connector === connectors[conID]) {
					defer { cb.view.enabled_(changer.value[conID].isNil )}
				}
			}
		})
	}
}

OscResolutionBox : ConnectorElementView {
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

		this.prMCDistinct(\osc);
		this.view = NumberBox(parentView, rect).clipLo_(0.01).scroll_step_(0.1).step_(0.1);
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
		connector = connectors[connectorID];
		optionsM.value[connectorID] !? {
			this.view.value_(connector.getOscResolution)
		}
	}

	setWidget { |otherWidget, slot|
		all[otherWidget] ?? { all[otherWidget] = List[] };
		all[otherWidget].add(this);
		this.prCleanup;
		// switch after cleanup has finished
		widget = otherWidget;
		this.slot_(slot);
		this.prMCDistinct(\osc);
		this.index_(0);
		// oscConnector at index 0 should always exist (who knows...)
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
					defer { nb.view.value_(changer.value[conID].oscResolution) }
				}
			}
		})
	}
}

OscSnapDistanceNumBox : ConnectorElementView {
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

		this.prMCDistinct(\osc);
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
		connector = connectors[connectorID];
		optionsM.value[connectorID] !? {
			this.view.value_(connector.getOscSnapDistance)
		}
	}

	setWidget { |otherWidget, slot|
		all[otherWidget] ?? { all[otherWidget] = List[] };
		all[otherWidget].add(this);
		this.prCleanup;
		// switch after cleanup has finished
		widget = otherWidget;
		this.slot;
		this.prMCDistinct(\osc);
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
					defer { nb.view.value_(changer.value[conID].oscSnapDistance) }
				}
			}
		})
	}
}

OscConstrainterNumBox : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;
	var cv, position;

	*initClass {
		all = ()
	}

	*new { |parent, widget, rect, slot, connectorID(0), position|
		if (widget.isKindOf(CVWidget).not) {
			Error("arg 'widget' must be a kind of CVWidget").throw
		};
		^super.newCopyArgs(widget: widget, slot: slot).init(parent, rect, connectorID, position)
	}

	init { |parentView, rect, index, pos|
		position = pos;
		all[widget] ?? { all[widget] = List[] };
		all[widget].add(this);

		this.prMCDistinct(\osc);

		cv = switch(position)
		{ 0 } { oscInputConstrainters[index].lo }
		{ 1 } { oscInputConstrainters[index].hi };

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
		connector = connectors[connectorID];
		optionsM.value[connectorID] !? {
			cv.disconnect(this.view);
			cv = switch(position)
			{ 0 } { oscInputConstrainters[connectorID].lo }
			{ 1 } { oscInputConstrainters[connectorID].hi };
			cv.connect(this.view);
		}
	}

	setWidget { |otherWidget, slot|
		all[otherWidget] ?? { all[otherWidget] = List[] };
		all[otherWidget].add(this);

		cv.disconnect(this.view);
		this.prCleanup;
		widget = otherWidget;
		this.slot_(slot);
		this.prMCDistinct(\osc);
		// oscConnector at index 0 should always exist (who knows...)
		this.index_(0);
		cv = switch(position)
		{ 0 } { widget.wmc.oscInputConstrainters[0].lo }
		{ 1 } { widget.wmc.oscInputConstrainters[0].hi };
		cv.connect(this.view);
	}
}

OscZeroCrossingText : ConnectorElementView {
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

		this.prMCDistinct(\osc);
		this.view = StaticText(parentView, rect)
		.string_(widget.getOscInputAlwaysPositive(index, this.slot))
		.minWidth_(30)
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
		connector = connectors[connectorID];
		optionsM.value[connectorID] !? {
			this.view.string_(connector.getOscInputAlwaysPositive)
		}
	}

	setWidget { |otherWidget, slot|
		all[otherWidget] ?? { all[otherWidget] = List[] };
		all[otherWidget].add(this);
		this.prCleanup;
		// switch after cleanup has finished
		widget = otherWidget;
		this.slot_(slot);
		this.prMCDistinct(\osc);
		this.index_(0);
		// oscConnector at index 0 should always exist (who knows...)
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
			// "OscZeroCrossingText:-prAddController: changer.value[%]: %".format(conID, changer.value[conID]).postln;
			all[widget].do { |st|
				if (st.connector === connectors[conID]) {
					defer {
						changer.value[conID].alwaysPositive !? {
							st.view.string_(changer.value[conID].alwaysPositive.round(0.01))
						}
					}
				}
			}
		})
	}
}

OscCalibrationButton : ConnectorElementView {
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

		this.prMCDistinct(\osc);
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
		connector = connectors[connectorID];
		optionsM.value[connectorID] !? {
			this.view.value_(connector.getOscCalibration.asInteger)
		}
	}

	setWidget { |otherWidget, slot|
		// FIXME: check for CVWidget2D slot (once it's implemented...)
		if (otherWidget.class !== CVWidgetKnob) {
			Error("Widget must be a CVWidgetKnob").throw
		};

		all[otherWidget] ?? { all[otherWidget] = List[] };
		all[otherWidget].add(this);
		this.prCleanup;
		// switch after cleanup has finished
		widget = otherWidget;
		this.slot_(slot);
		this.prMCDistinct(\osc);
		this.index_(0);
		// oscConnector at index 0 should always exist (who knows...)
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
			all[widget].do { |bt|
				if (bt.connector === connectors[conID]) {
					defer { bt.view.value_(changer.value[conID].oscCalibration.asInteger) }
				}
			}
		})
	}
}

OscCalibrationResetButton : ConnectorElementView {
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

		this.prMCDistinct(\osc);
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
		connector = connectors[connectorID];
		optionsM.value[connectorID] !? {
			this.view.value_(connector.getOscCalibration.asInteger)
		}
	}

	setWidget { |otherWidget, slot|
		all[otherWidget] ?? { all[otherWidget] = List[] };
		all[otherWidget].add(this);
		this.prCleanup;
		// switch after cleanup has finished
		widget = otherWidget;
		this.slot_(slot);
		this.prMCDistinct(\osc);
		this.index_(0);
		// oscConnector at index 0 should always exist (who knows...)
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
			all[widget].do { |bt|
				if (bt.connector === connectors[conID]) {
					defer { bt.view.value_(changer.value[conID].oscCalibration.asInteger) }
				}
			}
		})
	}
}

OscConnectButton : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;
	var validOsc = "^/[\\w\\d\\H/]+[\\w\\d\\H]+[^/\\h]$";

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
		var ip, port, addr, cmd, cmdIndex, matching, argTemplate, dispatcher;
		var conID;

		all[widget] ?? { all[widget] = List[] };
		all[widget].add(this);

		this.prMCDistinct(\osc);

		case
		{ displayM.value[index].nameField === '/path/to/cmd' or: {
			validOsc.matchRegexp(displayM.value[index].nameField.asString).not
		}} {
			defaultState = ["learn", Color.white, Color.blue]
		}
		// check https://www.boost.org/doc/libs/1_69_0/libs/regex/doc/html/boost_regex/syntax/perl_syntax.html
		{ validOsc.matchRegexp(displayM.value[index].nameField.asString) } {
			defaultState = ["connect", Color.black, Color.green]
		};

		this.view = Button(parentView, rect);
		this.view.onClose_({ this.close });
		this.index_(index);
		this.view.states_([
			defaultState,
			[displayM.value[index].disconnect, Color.white, Color.red]
		]);

		this.view.action_({ |bt|
			conID = connector.index;
			// "connectionsM.value[%].isNil? %".format(conID, connectionsM.value[conID].isNil).postln;
			if (connectionsM.value[conID].isNil) {
				if (displayM.value[conID].ipField.notNil) {
					ip = displayM.value[conID].ipField.asString;
					port = displayM.value[conID].portField;
					addr = NetAddr(ip, port);
				};
				cmd = this.connector.getOscCmdName;
				cmdIndex = this.connector.getOscMsgIndex;
				matching = this.connector.getOscMatching;
				argTemplate = this.connector.getOscTemplate;
				dispatcher = this.connector.getOscDispatcher;
				if (displayM.value[conID].learn) {
					OSCFunc.cvWidgetLearn(widget, this.slot, conID, matching, NetAddr.langPort, argTemplate, dispatcher);
				} {
					this.connector.oscConnect(addr, cmd, cmdIndex, NetAddr.langPort, argTemplate, dispatcher, matching);
				}
			} {
				this.connector.oscDisconnect;
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
		connector = connectors[connectorID];
		this.view.value_(connectionsM.value[connectorID].notNil.asInteger);
	}

	setWidget { |otherWidget, slot|
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
		this.slot_(slot);
		this.prMCDistinct(\osc);
		defaultState = displayM.value[0].connect0;
		this.index_(0);
		this.prAddController;
	}

	prAddController {
		var pos, conID;
		var cButFG, cButBG;

		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		// optionsC.put(syncKey, { |changer, what ... moreArgs|
		// 	conID = moreArgs[0];
		// 	all[widget].do { |bt, i|
		// 		if (bt.connector === connectors[conID]) {
		// 			// "oscOptions controller: % (connector ID: %)".format(changer.value[conID], conID).postln
		// 		}
		// 	}
		// });
		displayC.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget].do { |bt, i|
				if (bt.connector === connectors[conID]) {
					defer {
						bt.view.states_([changer.value[conID].connectState]);
						bt.view.enabled_(changer.value[conID].connectEnabled);
						bt.view.toolTip_(changer.value[conID].connectWarning);
					}
				}
			}
		});
		// connectionsC.put(syncKey, { |changer, what ... moreArgs|
		// 	conID = moreArgs[0];
		// 	all[widget].do { |bt, i|
		// 		if (bt.connector === connectors[conID]) {
		// 			// switch (displayM.value[conID].connect)
		// 			// { "learn" } {
		// 			// 	cButFG = Color.white;
		// 			// 	cButBG = Color.blue;
		// 			// }
		// 			// { "connect" } {
		// 			// 	cButFG = Color.black;
		// 			// 	cButBG = Color.green;
		// 			// };
		// 			// defer { bt.value_(changer.value[conID].notNil.asInteger)
		// 			// 	.states_([
		// 			// 		[displayM.value[conID].connect, cButFG, cButBG],
		// 			// 		[displayM.value[conID].disconnect, Color.white, Color.red]
		// 			// 	])
		// 			// }
		// 		}
		// 	}
		// })
	}
}