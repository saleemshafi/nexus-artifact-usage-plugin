Sonatype.Events.addListener('fileContainerInit', function(items) {
	items.push(new Sonatype.repoServer.ReverseDependencyPanel({
		name : 'reverseDependencyPanel',
		tabTitle : 'Reverse Dependencies',
		preferredIndex : 20
	}));
});
Sonatype.Events.addListener('fileContainerUpdate', function(artifactContainer,
		data) {
	var panel = artifactContainer.find('name', 'reverseDependencyPanel')[0];
	if (data == null || !data.leaf) {
		// panel.showArtifact(null, artifactContainer);
	} else {
		panel.showDependees(data, artifactContainer);
	}
});

Sonatype.Events.addListener('artifactContainerInit', function(items) {
	items.push(new Sonatype.repoServer.ReverseDependencyPanel({
		name : 'reverseDependencyPanel',
		tabTitle : 'Reverse Dependencies',
		preferredIndex : 20
	}));
});
Sonatype.Events.addListener('artifactContainerUpdate', function(
		artifactContainer, payload) {
	var panel = artifactContainer.find('name', 'reverseDependencyPanel')[0];
	if (payload == null || !payload.leaf) {
		// panel.showArtifact(null, artifactContainer);
	} else {
		panel.showDependees(payload, artifactContainer);
	}
});

Sonatype.repoServer.ReverseDependencyPanel = function(config) {
	var config = config || {};
	var defaultConfig = {};
	Ext.apply(this, config, defaultConfig);
	
	this.oldSearchText = '';
	this.searchTask = new Ext.util.DelayedTask( this.startSearch, this, [this]);
	
	Sonatype.repoServer.ReverseDependencyPanel.superclass.constructor
			.call(
					this,
					{
						title : 'Reverse Dependencies',
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
								' '
// Commenting this out 'cause it doesn't quite work the way it ought to.  Not
// sure that it even makes sense if the full tree isn't populated.
//								, 'Path Lookup:',
//								{
//									xtype : 'nexussearchfield',
//									searchPanel : this,
//									width : 400,
//									enableKeyEvents : true,
//									listeners : {
//										'keyup' : {
//											fn : function(field, event) {
//												var key = event.getKey();
//												if (!event.isNavKeyPress()) {
//													this.searchTask.delay(200);
//												}
//											},
//											scope : this
//										},
//										'render' : function(c) {
//											Ext.QuickTips
//													.register({
//														target : c.getEl(),
//														text : 'Enter a complete path to lookup, for example org/sonatype/nexus'
//													});
//										}
//									}
//								}
								],
						loader : new Ext.tree.SonatypeTreeLoader({
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
				Sonatype.repoServer.ReverseDependencyPanel,
				Ext.tree.TreePanel,
				{
					showDependees : function(payload, artifactContainer) {
						this.payload = payload;

						this.root.setText(payload.text);
						this.root.attributes.localStorageUpdated = false;
						this.root.id = payload.resourceURI.replace("/content/",
								"/dependees/");
						this.root.attributes.expanded = false;
					},

					indexBrowserExpandFollowup : function(node) {
						var urlBase = "/nexus/service/local/repo_groups/public/dependees/";
						for ( var j = 0; j < node.childNodes.length; j++) {
							var childNode = node.childNodes[j];
							childNode.id = urlBase+childNode.text;
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
						this.root.setText(this.payload.text);
						this.root.attributes.localStorageUpdated = false;
						this.root.attributes.expanded = false;
						this.root.id = this.payload.resourceURI.replace(
								"/content/", "/dependees/");
						this.root.reload();
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
					}

				});

