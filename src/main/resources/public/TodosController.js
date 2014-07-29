module.exports = TodosController;

//var counter = 1;
//var base = '' + Date.now() + Math.floor(Math.random() * 1000);

function TodosController(todos) {
	this.todos = todos;
}

TodosController.prototype.add = function(todo) {
//	todo.id = base + (counter++);
	this.todos.push(todo);
};

TodosController.prototype.remove = function(todo) {
	this.todos.some(function(t, i, todos) {
		if(todo.id === t.id) {
			todos.splice(i, 1);
			return true;
		}
	});
};

TodosController.prototype.removeCompleted = function() {
	this.todos = this.todos.filter(function(todo) {
		return !todo.complete;
	});
};

TodosController.prototype.completeAll = function() {
	this.todos.forEach(function(todo) {
		todo.complete = true;
	});
};
