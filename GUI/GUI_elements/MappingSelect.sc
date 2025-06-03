MappingSelect : CompositeView {
	classvar <all;
	var mc, connectors, syncKey;
	var <connector, <widget;
	var <e;
	var defaultEnv;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, connectorID=0, connectorKind, layout([[\mselect, \mcurve, \mplot], [\menv]])|
		^super.new.init(parent, widget, rect, connectorID, connectorKind, layout);
	}

	init { |parentView, wdgt, rect, index, connectorKind, layout|
		var parent, row, i;
		var ramp, env;

		defaultEnv = Env([0, 1], [1]);

		all[wdgt] ?? {
			all.put(wdgt, List[])
		};
		all[wdgt].add(this);

		widget = wdgt;

		connectorKind ?? {
			Error("arg 'connectorKind' in MappingSelect.new must not be nil - must either be 'midi' or 'osc'.").throw
		};
		connectorKind = connectorKind.asSymbol;


		case
		{ connectorKind === \midi } {
			mc = widget.wmc.midiInputMappings;
			connectors = widget.midiConnectors;
		}
		{ connectorKind === \osc } {
			mc = widget.wmc.oscInputMappings;
			connectors = widget.oscConnectors;
		};

		// the kind of connector must be known by now, so, index_ should already know

		if (parentView.isNil) {
			parent = Window("%: % mappings".format(widget.name, connectorKind.asString.toUpper), Rect(0, 0, 300, 65))
		} {
			parent = parentView;
		};

		this.background_(Color.green(alpha: 0.5)).minHeight_(80);

		ramp = switch (mc.model.value[index].mapping)
		{ \linenv } { mc.model.value[index].env }
		{ \lincurve } { [\lincurve, mc.model.value[index].curve] }
		{ \linbicurve } { [\linbicurve, mc.model.value[index].curve] }
		{ mc.model.value[index].mapping };

		e = ();
		e.mplot = RampPlot(parent, ramp: ramp).maxHeight_(25);
		e.mselect = PopUpMenu(parent).items_([
			\linlin, \linexp, \explin, \expexp, \lincurve, \linbicurve, \linenv
		]).minHeight_(25);
		e.mcurve = NumberBox(parent).clipHi_(12).clipLo_(-12).minHeight_(25);
		e.menv = TextField(parent).minHeight_(25)
		.string_((mc.model.value[index].env ? Env([0, 1], [1])).asCompileString);

		case
		{ mc.model.value[index].mapping === \lincurve or: { mc.model.value[index].mapping === \linbicurve }} {
			e.mcurve.enabled_(true);
			e.menv.enabled_(false);
		}
		{ mc.model.value[index].mapping === \linenv } {
			e.menv.enabled_(true);
			e.mcurve.enabled_(false);
		}
		{
			e.menv.enabled_(false);
			e.mcurve.enabled_(false);
		};


		if (layout.size > 1) {
			this.layout_(VLayout());
			layout.size.do { |i|
				row = HLayout();
				layout[i].do { |k| row.add(e[k]) };
				this.layout.add(row)
			};
		} {
			row = HLayout();
			layout[0].do { |k| row.add(e[k]) };
			this.layout.add(row)
		};

		this.index_(index);
		this.onClose_({ this.close });

		e.mselect.action_({ |sel|
			i = connectors.indexOf(this.connector);
			env = if (e.menv.string.interpret.class == Env) {
				e.menv.string.interpret
			} { defaultEnv };

			case
			{ sel.value == 4 or: { sel.value == 5 }} {
				mc.model.value[i].mapping = sel.items[sel.value];
				mc.model.value[i].curve = e.mcurve.value;
				mc.model.value[i].env = nil;
			}
			{ sel.value == 6 } {
				mc.model.value[i].env = env;
				mc.model.value[i].mapping = sel.items[sel.value];
				mc.model.value[i].curve = nil;
			}
			{
				mc.model.value[i].mapping = sel.items[sel.value];
				mc.model.value[i].env = nil;
				mc.model.value[i].curve = nil;
			};
			mc.model.changedPerformKeys(widget.syncKeys, i);
		});
		e.mcurve.action_({ |nb|
			i = connectors.indexOf(this.connector);
			if (e.mselect.value == 4 or: { e.mselect.value == 5 }) {
				mc.model.value[i].curve = nb.value;
				mc.model.changedPerformKeys(widget.syncKeys, i)
			}
		});
		e.menv.action_({ |tf|
			i = connectors.indexOf(this.connector);
			env = if (tf.string.interpret.class == Env) {
				tf.string.interpret
			} { defaultEnv };

			if (e.mselect.value == 6) {
				mc.model.value[i].env = env;
				mc.model.changedPerformKeys(widget.syncKeys, i)
			}
		});

		this.prAddController;
	}

	index_ { |connectorID|
		// "connectorID: %".format(connectorID).postln;
		connector = connectors[connectorID];
		e.mselect.value_(e.mselect.items.indexOf(mc.model.value[connectorID].mapping));
		e.mcurve.value_(mc.model.value[connectorID].curve ? 0);
		e.menv.string_((mc.model.value[connectorID].env ? defaultEnv).asCompileString);
		case
		{ mc.model.value[connectorID].mapping === \lincurve or: { mc.model.value[connectorID].mapping === \linbicurve }} {
			e.mplot.draw([mc.model.value[connectorID].mapping, mc.model.value[connectorID].curve]);
			e.menv.enabled_(false);
			e.mcurve.enabled_(true);
		}
		{ mc.model.value[connectorID].mapping === \linenv } {
			e.mplot.draw(mc.model.value[connectorID].env);
			e.menv.enabled_(true);
			e.mcurve.enabled_(false);
		}
		{
			e.mplot.draw(mc.model.value[connectorID].mapping);
			e.menv.enabled_(false);
			e.mcurve.enabled_(false);
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
				all[widget].do { |ms, i|
					if (ms.connector === connectors[conID]) {
						{
							ms.e.mselect.value_(e.mselect.items.indexOf(changer.value[conID].mapping));
							case
							{ changer.value[conID].mapping === \lincurve or: { changer.value[conID].mapping === \linbicurve }} {
								ms.e.mcurve.value_(changer.value[conID].curve).enabled_(true);
								ms.e.mplot.draw([changer.value[conID].mapping, changer.value[conID].curve]);
								ms.e.menv.enabled_(false);
							}
							{ changer.value[conID].mapping === \linenv } {
								ms.e.mcurve.enabled_(false);
								ms.e.mplot.draw(changer.value[conID].env ? defaultEnv);
								ms.e.menv.string_(changer.value[conID].env.asCompileString).enabled_(true);
							}
							{
								ms.e.mcurve.enabled_(false);
								ms.e.mplot.draw(changer.value[conID].mapping);
								ms.e.menv.enabled_(false);
							}
						}.defer
					}
				}
			})
		}
	}

	close {
		all[widget].remove(this);
		e.do(_.close);
		if (all[widget].isEmpty) {
			mc.controller.removeAt(syncKey);
			widget.prRemoveSyncKey(syncKey, true);
		}
	}

}

RampPlot : SCViewHolder {
	var <>background, <>foreground;

	*new { |parent, rect, ramp = \linlin, background(Color.blue(0.3)), foreground(Color.cyan)|
		^super.newCopyArgs(nil, background, foreground).init(parent, rect, ramp)
	}

	init { |parentView, rect, ramp|
		this.view = UserView(parentView, rect)
		.background_(this.background)
		.minWidth_(40)
		.minHeight_(25);
		this.draw(ramp);
		this.onClose_({ this.close });
	}

	draw { |ramp|
		var rampVals = this.prCreateRampVals(ramp);

		this.view.background_(this.background);
		this.view.drawFunc_({ |v|
			Pen.strokeColor_(this.foreground).width_(2).moveTo(0@this.view.bounds.height);
			(rampVals.size - 1).do { |i|
				Pen.lineTo(Point(
					this.view.bounds.width/rampVals.size*(i+1),
					this.view.bounds.height-(rampVals[i+1] * this.view.bounds.height)
				))
			};
			Pen.stroke;
		});
		this.view.refresh
	}

	prCreateRampVals { |ramp|
		// rampArray models a linear ramp;
		var rampArray = (0, 0.01..1);

		switch (ramp.class)
		{ Array } {
			if (ramp.size < 2) {
				"if arg 'ramp' is given as an Array it must contain two values: the first one denoting the mapping method (\\lincurve or \\linbicurve) and the second one a number denoting the curve caracteristics".error;
				^rampArray;
			} {
				ramp[0] = ramp[0].asSymbol;
				if (ramp[1].isNumber) {
					switch (ramp[0])
					{ \lincurve } {
						^rampArray.perform(ramp[0], 0, 1, 0, 1, ramp[1])
					}
					{ \linbicurve } {
						^rampArray.perform(ramp[0], 0, 1, nil, 0, 1, nil, ramp[1])
					}
				}
			}
		}
		{ Env } {
			if (ramp.respondsTo(\asMultichannelSignal).not) {
				"No valid envelope given - defaulting to Env([0, 1], [1], \lin)".error;
				^rampArray
			} {
				^rampArray.linenv(0, 1, 0, 1, ramp, \minmax, rampArray.size)
			}
		}
		{ ^rampArray.perform(ramp.asSymbol, 0.02, 1, 0.02, 1) }
	}

}