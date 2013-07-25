var createArtifactUsagePanels = function(items) {
	var treePanel = new Sonatype.repoServer.ArtifactUsageTreePanel({
		name : 'artifactUsageTreePanel',
		tabTitle : 'Artifact Usage',
		preferredIndex : 30
	});
	var listPanel = new Sonatype.repoServer.ArtifactUsageListPanel({
		name : 'artifactUsageListPanel',
		tabTitle : 'Artifact Usage',
		preferredIndex : 31
	});
	
	treePanel.setTogglePartner(listPanel);
	items.push(treePanel);
	items.push(listPanel);
};

var renderArtifactUsagePanels = function(artifactContainer, payload) {
	var listPanel = artifactContainer.find('name', 'artifactUsageListPanel')[0];
	var treePanel = artifactContainer.find('name', 'artifactUsageTreePanel')[0];
	if (payload == null || !payload.leaf) {
		// panel.showArtifactUsageData(null, artifactContainer);
	} else {
		treePanel.toggleContainer = listPanel.toggleContainer = artifactContainer;
		treePanel.loadData(payload, artifactContainer);
		listPanel.loadData(payload, artifactContainer);
		if (treePanel.toggleOn) {
			artifactContainer.hideTab(listPanel);
			artifactContainer.showTab(treePanel);
		}
		if (listPanel.toggleOn) {
			artifactContainer.hideTab(treePanel);
			artifactContainer.showTab(listPanel);
		}
	}
}

Sonatype.Events.addListener('fileContainerInit', createArtifactUsagePanels);
Sonatype.Events.addListener('artifactContainerInit', createArtifactUsagePanels);

Sonatype.Events.addListener('fileContainerUpdate', renderArtifactUsagePanels);
Sonatype.Events.addListener('artifactContainerUpdate', renderArtifactUsagePanels);

Sonatype.repoServer.ArtifactUsageTreePanel = function(config) {
	var config = config || {};
	var defaultConfig = {};
	Ext.apply(this, config, defaultConfig);

	this.oldSearchText = '';
	this.searchTask = new Ext.util.DelayedTask(this.startSearch, this, [ this ]);

	Sonatype.repoServer.ArtifactUsageTreePanel.superclass.constructor.call(this, {
		title : 'Artifact Usage',
		anchor : '0 -2',
		bodyStyle : 'background-color:#FFFFFF',
		animate : true,
		lines : false,
		autoScroll : true,
		containerScroll : true,
		rootVisible : true,
		enableDD : false,
		tbar : [
				{
					text : 'Refresh',
					icon : Sonatype.config.resourcePath
							+ '/images/icons/arrow_refresh.png',
					cls : 'x-btn-text-icon',
					scope : this,
					handler : this.refreshHandler
				},
				' ',
				{
					text : 'View as List',
					cls : 'x-btn-text',
					scope : this,
					handler : this.toggleView
				},
				' ',
				{
					text : 'Download Tree As XML',
					icon : Sonatype.config.resourcePath
							+ '/images/icons/page_white_put.png',
					cls : 'x-btn-text-icon',
					scope : this,
					handler : this.downloadUsageTree
				},

				// TODO: need to be able to filter by scope, repository,
				// dependencyType (dependency, plugin, reporting plugin)
				' '
		// Commenting this out 'cause it doesn't quite work the way it ought to.
		// Not
		// sure that it even makes sense if the full tree isn't populated.
		// , 'Path Lookup:',
		// {
		// xtype : 'nexussearchfield',
		// searchPanel : this,
		// width : 400,
		// enableKeyEvents : true,
		// listeners : {
		// 'keyup' : {
		// fn : function(field, event) {
		// var key = event.getKey();
		// if (!event.isNavKeyPress()) {
		// this.searchTask.delay(200);
		// }
		// },
		// scope : this
		// },
		// 'render' : function(c) {
		// Ext.QuickTips
		// .register({
		// target : c.getEl(),
		// text : 'Enter a complete path to lookup, for example
		// org/sonatype/nexus'
		// });
		// }
		// }
		// }
		],
		loader : new Ext.tree.SonatypeMultiLevelTreeLoader({
			url : '',
			listeners : {
				loadexception : this.treeLoadExceptionHandler,
				scope : this
			}
		}),
		listeners : {
			click : this.nodeClickHandler,
			// remove existing right-click menu
			// contextMenu: this.nodeContextMenuHandler,
			expandnode : this.indexBrowserExpandFollowup,
      dblclick: {
        fn: this.search,
        scope: this
      },
			scope : this
		}
	});

	new Ext.tree.TreeSorter(this, {
		folderSort : true
	});

	var root = new Ext.tree.AsyncTreeNode({
		text : "error",
		id : "error-node",
		singleClickExpand : true,
		expanded : false
	});

	this.setRootNode(root);
};

Ext
		.extend(
				Sonatype.repoServer.ArtifactUsageTreePanel,
				Ext.tree.TreePanel,
				{
					loadData : function(payload, artifactContainer) {
						var resourceURI = payload.resourceURI;
						if (resourceURI != this.resourceURI) {
							this.resourceURI = resourceURI;
							Ext.Ajax
							.request({
								url : resourceURI
										+ '?describe=maven2&isLocal=true',
								callback : function(options, isSuccess,
										response) {
									if (isSuccess) {
										artifactContainer.showTab(this);
										var infoResp = Ext
												.decode(response.responseText);
										this
												.showArtifactUsers(infoResp.data);
									} else {
										if (response.status = 404) {
											artifactContainer.hideTab(this);
										} else {
											Sonatype.utils
												.connectionError(
														response,
														'Unable to retrieve Maven information.');
										}
									}
								},
								scope : this,
								method : 'GET',
								suppressStatus : '404'
							});
						}
					},

					showArtifactUsers : function(rootArtifact) {
						this.rootArtifact = rootArtifact;
						var gav = rootArtifact.groupId + ":"
								+ rootArtifact.artifactId + ":"
								+ rootArtifact.baseVersion;
						this.root.setText(gav);
						this.root.attributes.localStorageUpdated = false;
						this.root.attributes.expanded = false;
						this.root.id = Sonatype.config.servicePath+"/usage/"
								+ this.root.text;
						this.root.reload();
					},

					indexBrowserExpandFollowup : function(node) {
						var urlBase = Sonatype.config.servicePath+"/usage/";
						for ( var j = 0; j < node.childNodes.length; j++) {
							var childNode = node.childNodes[j];
							childNode.id = urlBase + childNode.text;
						}
					},

					nodeClickHandler : function(node, e) {
						if (e.target.nodeName == 'A')
							return; // no menu on links

						if (this.nodeClickEvent) {
							Sonatype.Events
									.fireEvent(this.nodeClickEvent, node);
						}
					},

					nodeContextMenuHandler : function(node, e) {
						// TODO: Put context menu logic here. Not sure what we
						// actually
						// need here, though. Maybe an option to go to the
						// artifact details?
						if (e.target.nodeName == 'A')
							return; // no menu on links

						if (this.nodeContextMenuEvent) {

							node.attributes.repoRecord = null; // this.payload;
							node.data = node.attributes;

							var menu = new Sonatype.menu.Menu({
								id : 'repo-context-menu',
								payload : node,
								scope : this,
								items : []
							});

							// Sonatype.Events.fireEvent(
							// this.nodeContextMenuEvent, menu,
							// , node);

							var item;
							while ((item = menu.items.first()) && !item.text) {
								menu.remove(item); // clean up if the first
								// element is a separator
							}
							while ((item = menu.items.last()) && !item.text) {
								menu.remove(item); // clean up if the last
								// element is a separator
							}
							if (!menu.items.first())
								return;

							e.stopEvent();
							menu.showAt(e.getXY());
						}
					},

					refreshHandler : function(button, e) {
						this.showArtifactUsers(this.rootArtifact);
					},

					downloadUsageTree : function(button, e) {
						Sonatype.utils
								.openWindow("/nexus/service/local/usageGraph/"
										+ this.root.text + ".xml");
					},

					startSearch : function(p) {
						var field = p.searchField;
						var searchText = field.getRawValue();

						var treePanel = p;
						if (searchText) {
							field.triggers[0].show();
							var justEdited = p.oldSearchText.length > searchText.length;

							var findMatchingNodes = function(root, textToMatch) {
								var n = textToMatch.indexOf('/');
								var remainder = '';
								if (n > -1) {
									remainder = textToMatch.substring(n + 1);
									textToMatch = textToMatch.substring(0, n);
								}

								var matchingNodes = [];
								var found = false;
								for ( var i = 0; i < root.childNodes.length; i++) {
									var node = root.childNodes[i];

									var text = node.text;
									if (text == textToMatch) {
										node.enable();
										node.ensureVisible();
										node.expand();
										found = true;
										if (!node.isLeaf()) {
											var autoComplete = false;
											if (!remainder
													&& node.childNodes.length == 1) {
												remainder = node.firstChild.text;
												autoComplete = true;
											}
											if (remainder) {
												var s = findMatchingNodes(node,
														remainder);
												if (autoComplete
														|| (s && s != remainder)) {
													return textToMatch
															+ '/'
															+ (s ? s
																	: remainder);
												}
											}
										}
									} else if (text.substring(0,
											textToMatch.length) == textToMatch) {
										matchingNodes[matchingNodes.length] = node;
										node.enable();
										if (matchingNodes.length == 1) {
											node.ensureVisible();
										}
									} else {
										node.disable();
										node.collapse(false, false);
									}
								}

								// if only one non-exact match found, suggest
								// the name
								return !found && matchingNodes.length == 1 ? matchingNodes[0].text
										+ '/'
										: null;
							};

							var s = findMatchingNodes(treePanel.root,
									searchText);

							p.oldSearchText = searchText;

							// if auto-complete is suggested, and the user
							// hasn't just started deleting
							// their own typing, try the suggestion
							if (s && !justEdited && s != searchText) {
								field.setRawValue(s);
								p.startSearch(p);
							}

						} else {
							p.stopSearch(p);
						}
					},

					stopSearch : function(p) {
						p.searchField.triggers[0].hide();
						p.oldSearchText = '';

						var treePanel = p;

						var enableAll = function(root) {
							for ( var i = 0; i < root.childNodes.length; i++) {
								var node = root.childNodes[i];
								node.enable();
								node.collapse(false, false);
								enableAll(node);
							}
						};
						enableAll(treePanel.root);
					},

					treeLoadExceptionHandler : function(treeLoader, node,
							response) {
						if (response.status == 503) {
							if (Sonatype.MessageBox.isVisible()) {
								Sonatype.MessageBox.hide();
							}
							node.setText(node.text + ' (Out of Service)');
						} else if (response.status == 404
								|| response.status == 400) {
							if (Sonatype.MessageBox.isVisible()) {
								Sonatype.MessageBox.hide();
							}
							node.setText(node.text
									+ (node.isRoot ? ' (Not Available)'
											: ' (Not Found)'));
						} else if (response.status == 401
								|| response.status == 403) {
							if (Sonatype.MessageBox.isVisible()) {
								Sonatype.MessageBox.hide();
							}
							node.setText(node.text + ' (Access Denied)');
						}
					},

          search: function (node, event) {
            var groupId = node.attributes.groupId;
            var artifactId = node.attributes.artifactId;
            var version = node.attributes.version;
            window.location = "index.html#nexus-search;gav~" + groupId + "~" + artifactId + "~" + version + "~~~";
          }

				});

Ext.tree.SonatypeMultiLevelTreeLoader = function(config) {
	Ext.tree.SonatypeMultiLevelTreeLoader.superclass.constructor.call(this,
			config)
};
Ext.extend(Ext.tree.SonatypeMultiLevelTreeLoader, Ext.tree.SonatypeTreeLoader,
		{
			processResponse : function(response, node, callback) {
				var json = response.responseText;
				try {
					var o = eval("(" + json + ")");
					node.beginUpdate();
					this.addNodes(node, o.data);
					node.endUpdate();
					if (typeof callback == "function") {
						callback(this, node)
					}
				} catch (e) {
					this.handleFailure(response)
				}
			},
			addNodes : function(node, o) {
				if (this.jsonRoot) {
					o = o[this.jsonRoot]
				}
				for ( var i = 0, len = o.length; i < len; i++) {
					var n = this.createNode(o[i]);
					if (n) {
            n.setIcon("icons/repoServer/jar-jar.png");
						node.appendChild(n)
						if (o[i].data) {
							this.addNodes(n, o[i].data);
							n.loading = false;
							n.loaded = true;
						}
					}
				}
			}
		});

Sonatype.repoServer.ArtifactUsageListPanel = function(config) {
	var config = config || {};
	var defaultConfig = {
		title : "Artifact Usage"
	};
	Ext.apply(this, config, defaultConfig);
	
	Sonatype.repoServer.ArtifactUsageListPanel.superclass.constructor.call(this, {
		dataAutoLoad : false,
		dataId : "id",
		dataBookmark : "id",
		dataSortInfo : {
			field : "groupId",
			direction : "asc"
		},
		columns : [ {
			name : "groupId",
			header : "Group ID",
			width : 200
		}, {
			name : "id"
		}, {
			name : "artifactId",
			header : "Artifact ID",
			width : 250,
		}, {
			name : "version",
			header : "Version",
			width : 175
		} ],
		tbar : [
				{
					text : 'View as Tree',
					cls : 'x-btn-text',
					scope : this,
					handler : this.toggleView
				},
				' '
				]
	});
}

Ext.extend(Sonatype.repoServer.ArtifactUsageListPanel, Sonatype.panels.GridViewer, {
	loadData : function(payload, artifactContainer) {
		resourceURI = payload.resourceURI;
		if (resourceURI != this.resourceURI) {
			this.resourceURI = resourceURI;
			Ext.Ajax
					.request({
						url : resourceURI
								+ '?describe=maven2&isLocal=true',
						callback : function(options, isSuccess,
								response) {
							if (isSuccess) {
								var infoResp = Ext
										.decode(response.responseText);
								this
										.showArtifactUsers(infoResp.data);
							} else {
								if (response.status = 404) {
									artifactContainer.hideTab(this);
								} else {
									Sonatype.utils
										.connectionError(
												response,
												'Unable to retrieve Maven information.');
								}
							}
						},
						scope : this,
						method : 'GET',
						suppressStatus : '404'
					});
		}
	},

	showArtifactUsers : function(rootArtifact) {
		this.rootArtifact = rootArtifact;
		var gav = rootArtifact.groupId + ":"
				+ rootArtifact.artifactId + ":"
				+ rootArtifact.baseVersion;

		this.url = Sonatype.config.servicePath+"/usageList/"+gav,
		this.dataStore.url = this.url;
		this.dataStore.proxy = new Ext.data.HttpProxy({
			url : this.url
		});
		this.dataAutoLoad = true;

		this.dataStore.on("load", this.dataStoreLoadHandler, this);
		this.dataStore.load()
	},

});

Sonatype.repoServer.ViewToggle =  {
		toggleOn : false,
		toggler : null,
		toggleContainer : null,
		toggleView : function() {
			if (this.toggler && this.toggleContainer && this.toggleOn) {
				this.toggleContainer.hideTab(this);
				this.toggleOn = false;
				this.toggleContainer.showTab(this.toggler);
				this.toggler.show();
				this.toggler.toggleOn = true;
			}
		},
		
		setTogglePartner : function(toggler) {
			this.toggler = toggler;
			toggler.toggler = this;
			this.toggleOn = true;  // need to do this so the toggleView works
		}					
};

Ext.applyIf(Sonatype.repoServer.ArtifactUsageTreePanel.prototype, Sonatype.repoServer.ViewToggle);
Ext.applyIf(Sonatype.repoServer.ArtifactUsageListPanel.prototype, Sonatype.repoServer.ViewToggle);
