var fab = require('fabulous');
var rest = require('fabulous/rest');
var Document = require('fabulous/Document');
var PatchClient = require('fabulous/data/PatchClient');

var TodosController = require('./TodosController');

exports.main = fab.run(document.body, todosApp);

function todosApp(node, context) {
	context.controller = new TodosController([]);

	Document.sync([
		new PatchClient(rest.at('/todos')),
		Document.fromProperty('todos', context.controller)
	], context.scheduler);
}
