ConnectorElementView : SCViewHolder {
	var mc, syncKey;
	var toolTip;

	close {
		this.remove;
		this.viewDidClose;
		this.prCleanup;
	}

	prCleanup {
		this.class.all[this.widget].remove(this);
		if (this.class.all[this.widget].isEmpty) {
			mc.controller.removeAt(syncKey);
			this.widget.prRemoveSyncKey(syncKey, true);
		}
	}

	prOnRemoveConnector { |index, connectorKind|
		var connectors;

		"%: prOnRemoveConnector called: %".format(this.class, connectorKind).postln;
		switch (connectorKind)
		{ \midi } { connectors = this.widget.midiConnectors }
		{ \osc } { connectors = this.widget.oscConnectors };

		[index, this.class, this.class.all[this.widget]].postln;
		if (index > 0) {
			this.class.all[this.widget].do(_.index_(index - 1))
		} {
			this.class.all[this.widget].do(_.index_(index))
		}
	}
}
