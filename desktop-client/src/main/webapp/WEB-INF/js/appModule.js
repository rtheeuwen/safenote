'use strict';
var appModule = angular.module("appModule", []);

appModule.controller("LoginController", ['$scope', 'AuthenticationService', function ($scope, AuthenticationService) {
    $scope.password = '';

    loginCursor();

    function loginCursor() {
        document.getElementById("passInput").select();
    }

    $scope.login=function (keyEvent) {
        if (keyEvent.which === 13) {
            $scope.loading = true;
            AuthenticationService.authenticate($scope, $scope.password);
            document.getElementById("passInput").select();
        }
    };

    $scope.clearLogin = function(){
        $scope.password = '';
        $scope.wrongpass = false;
    }
}]);

appModule.controller("KeyController", ['$scope', 'KeyService', function ($scope, KeyService) {
    $scope.password='';
    $scope.confirm='';

    $scope.generate = function generate() {
        if(($scope.password==$scope.confirm)&&($scope.password!='')) {
            KeyService.generate($scope.password);
        }
    };

    $scope.press=function (keyEvent) {
        if (keyEvent.which === 13) {
            console.log('enter');
            generate();
        }
    };
}]);

appModule.controller("NotesController", ['$scope', 'NoteService', function ($scope, NoteService) {

    $scope.headers = [];
    $scope.active = -1;
    $scope.text = 'Welcome to SafeNote';
    $scope.title = '';
    $scope.wordCount = 0;
    $scope.charCount = 0;
    $scope.modified = '';
    $scope.newHeader = 'New...';
    $scope.searchArgs = 'Search...';
    $scope.synchronized = true;

    function Note(id, content, header, wordCount, charCount, modified) {
        this.id = id;
        this.content = content;
        this.header = header;
        this.wordCount = wordCount;
        this.charCount = charCount;
        this.modified = modified;
    }

    getHeaders();


    $scope.open = function(index){
        //if another note is displayed on screen, save it
        if($scope.active!=-1) {
            var previous = new Note($scope.active, $scope.text, $scope.title, getWordCount($scope.text), getChars($scope.text), $scope.modified);
            NoteService.updateNote(previous, $scope.active);
        }//open note
        var id = $scope.headers[index].id;
        if($scope.active!=id) {
            NoteService.getNote(id).then(
                function (d) {
                    $scope.active = d.id;
                    $scope.text = d.content;
                    $scope.title = d.header;
                    $scope.wordCount = d.wordCount;
                    $scope.charCount = d.charCount;
                    $scope.modified = d.modified;
                },
                function (errResponse) {
                    console.error('error');
                }
            );
        };
    };

    $scope.create=function (keyEvent) {
        if (keyEvent.which === 13) {
            if($scope.active!=-1) {
                var previous = new Note($scope.active, $scope.text, $scope.title, getWordCount($scope.text), getChars($scope.text), $scope.modified);
                NoteService.updateNote(previous, $scope.active);
            }
            NoteService.createNote($scope.newHeader).then(
                //open
                function (d) {
                    var id=d;
                    getHeaders();
                    NoteService.getNote(id).then(
                        function (d) {
                            $scope.active = d.id;
                            $scope.text = d.content;
                            $scope.title = d.header;
                            $scope.wordCount=d.wordCount;
                            $scope.charCount = d.charCount;
                            $scope.modified = d.modified;
                            document.getElementById("editor").select();
                        },
                        function (errResponse) {
                            console.error('error');
                        }
                    );
                },
                function (errResponse) {
                    console.error('error');
                }
            );
            $scope.title = $scope.newHeader;
            $scope.wordCount = 0;
            $scope.charCount = 0;
            $scope.resetNewText();
            document.getElementById("headerInput").blur();

        }
    };

    $scope.searchEnter=function (keyEvent) {
        if (keyEvent.which === 13) {
            $scope.search();
            document.getElementById("search").blur();
        }
    };

    $scope.search=function () {
        if($scope.active!=-1) {
            var previous = new Note($scope.active, $scope.text, $scope.title, getWordCount($scope.text), getChars($scope.text), $scope.modified);
            NoteService.updateNote(previous, $scope.active).then(
                function (d) {
                    if($scope.searchArgs!='Search...') {
                        NoteService.search($scope.searchArgs).then(
                            function (d) {
                                $scope.headers = d;
                            },
                            function (errResponse) {
                                console.error('error');
                            }
                        );
                    } else {
                        NoteService.search('').then(
                            function (d) {
                                $scope.headers = d;
                            },
                            function (errResponse) {
                                console.error('error');
                            }
                        );
                    }
                },
                function (errResponse) {
                    console.error('error');
                }
            );
        } else {
            if ($scope.searchArgs != 'Search...') {
                NoteService.search($scope.searchArgs).then(
                    function (d) {
                        $scope.headers = d;
                    },
                    function (errResponse) {
                        console.error('error');
                    }
                );
            } else {
                NoteService.search('').then(
                    function (d) {
                        $scope.headers = d;
                    },
                    function (errResponse) {
                        console.error('error');
                    }
                );
            }
        }
    }

    $scope.delete=function() {
        if($scope.active!=-1){
                NoteService.deleteNote($scope.active).then(
                function (d) {
                    getHeaders();
                },
                function (errResponse) {
                    console.error('error');
                }
                );
                $scope.active = -1;
                $scope.text = $scope.title+' deleted';
                $scope.title = '';
                $scope.wordCount=0;
                $scope.charCount = 0;
                $scope.modified = '';
        }
    };

    function getHeaders(){
        NoteService.getHeaders().then(
            function (d) {
                $scope.headers=d;
            },
            function (errResponse) {
                console.error('error');
            }
        );
    }

    window.onbeforeunload = function (event) {
        var previous = new Note($scope.active, $scope.text, $scope.title, getWordCount($scope.text), getChars($scope.text), $scope.modified);
        NoteService.updateNote(previous, $scope.active);
        event = window.event;
        event.returnValue = "a";
        return "a";
    };

    $scope.newText = function () {
        $scope.newHeader = '';
    };

    $scope.resetNewText = function(){
        $scope.newHeader = 'New...';
    };

    $scope.clickSearch = function () {
        $scope.searchArgs = '';
    }

    $scope.resetSearchArgs = function () {
        if($scope.searchArgs=='') {
            $scope.searchArgs = 'Search...';
            $scope.search();
        }
    }

    $scope.clearSearchArgs = function () {
        $scope.searchArgs = 'Search...';
        $scope.search();
    }

    $scope.synchronize= function() {
        $scope.synchronized = false;
        if ($scope.active != -1) {
            var previous = new Note($scope.active, $scope.text, $scope.title, getWordCount($scope.text), getChars($scope.text), $scope.modified);
            NoteService.updateNote(previous, $scope.active).then(
                function (d) {
                    NoteService.getNote($scope.active).then(
                        function (d) {
                            $scope.active = d.id;
                            $scope.text = d.content;
                            $scope.title = d.header;
                            $scope.wordCount = d.wordCount;
                            $scope.charCount = d.charCount;
                            $scope.modified = d.modified;
                            NoteService.sync().then(
                                function (d) {
                                    $scope.synchronized = true;
                                    console.log('done');
                                },
                                function (errResponse) {
                                    console.error('error');
                                }
                            );
                        },
                        function (errResponse) {
                            console.error('error');
                        }
                    );
                }
            );
            $scope.searchArgs = 'Search...';
            getHeaders();
        } else
            NoteService.sync().then(
                function (d) {
                    console.log('done');
                    getHeaders();
                    $scope.synchronized = true;
                },
                function (errResponse) {
                    console.error('error');
                }
            );
    } ;

    $scope.about = function () {
        if ($scope.active != -1) {
            var previous = new Note($scope.active, $scope.text, $scope.title, getWordCount($scope.text), getChars($scope.text), $scope.modified);
            NoteService.updateNote(previous, $scope.active);
        }
        $scope.active=-1;
        $scope.title='';
        NoteService.getInfo().then(
            function (d) {
                $scope.text=d;
            },
            function (errResponse) {
                $scope.text='Error';
            }
        );
    }

    function getWordCount(string){
        if(string==undefined||string.length==0){
            return 0;
        } else
            return string.split(" ").length + string.split(/\r\n|\r|\n/).length -1
    }

    function getChars(string) {
        if(string==undefined){
            return 0;
        } else
            return string.length;
    }

}]);

appModule.factory('AuthenticationService', ['$http', '$q', function($http, $q){

    var REST_SERVICE_URI = 'http://localhost:47504/authentication';

    var factory = {
        authenticate: authenticate
    };

    return factory;

    function authenticate($scope, passphrase) {
        var deferred = $q.defer();
        $http.post(REST_SERVICE_URI , '"'+passphrase+'"')
            .then(
                function (response) {
                    deferred.resolve(response.data);
                    window.location='notes.html';
                },
                function(errResponse){
                    console.log('error');
                    $scope.loading = false;
                    $scope.wrongpass = true;
                    deferred.reject(errResponse);
                }
            );
        return deferred.promise;
    }

}]);

appModule.factory('KeyService', ['$http', '$q', function($http, $q){

    var REST_SERVICE_URI = 'http://localhost:47504/authentication';

    var factory = {
        generate: generate
    };

    return factory;

    function generate(passphrase) {
        var deferred = $q.defer();
        $http.post(REST_SERVICE_URI , '"'+passphrase+'"')
            .then(
                function (response) {
                    deferred.resolve(response.data);
                    window.location='notes.html';
                },
                function(errResponse){
                    console.log('error');
                    deferred.reject(errResponse);
                }
            );
        return deferred.promise;
    }

}]);

appModule.factory('NoteService', ['$http', '$q', function($http, $q){

    var REST_SERVICE_URI = 'http://localhost:47504/';

    var factory = {
        getHeaders: getHeaders,
        getNote: getNote,
        updateNote: updateNote,
        createNote: createNote,
        deleteNote: deleteNote,
        getInfo: getInfo,
        search: search,
        sync: sync
    };

    return factory;

    function getHeaders(){
        var deferred = $q.defer();
        $http.get(REST_SERVICE_URI+'headers')
            .then(
                function (response) {
                    deferred.resolve(response.data);
                },
                function (errResponse) {
                    console.error('Error while getting headers.')
                    deferred.reject(errResponse);
                }
            );
        return deferred.promise;
    }

    function search(args){
        var deferred = $q.defer();
        $http.get(REST_SERVICE_URI+'search?q='+args)
            .then(
                function (response) {
                    deferred.resolve(response.data);
                },
                function (errResponse) {
                    console.error('Error while getting headers.')
                    deferred.reject(errResponse);
                }
            );
        return deferred.promise;
    }

    function getNote(id){
        var deferred = $q.defer();
        $http.get(REST_SERVICE_URI+'notes/'+id)
            .then(
                function (response) {
                    deferred.resolve(response.data);
                },
                function (errResponse) {
                    console.error('Error while getting note.')
                    deferred.reject(errResponse);
                }
            );
        return deferred.promise;
    }

    function updateNote(note, id) {
        var deferred = $q.defer();
        $http.put(REST_SERVICE_URI+'notes/'+ id, note)
            .then(
                function (response) {
                    deferred.resolve(response.data);
                },
                function(errResponse){
                    console.error('Error while updating');
                    deferred.reject(errResponse);
                }
            );
        return deferred.promise;
    }

    function createNote(header) {
        var deferred = $q.defer();
        $http.post(REST_SERVICE_URI+'notes', '"'+ header +'"')
            .then(
                function (response) {
                    deferred.resolve(response.data);
                },
                function(errResponse){
                    console.error('Error while creating');
                    deferred.reject(errResponse);
                }
            );
        return deferred.promise;
    }

    function deleteNote(id) {
        var deferred = $q.defer();
        $http.delete(REST_SERVICE_URI+'notes/'+id)
            .then(
                function (response) {
                    deferred.resolve(response.data);
                },
                function(errResponse){
                    console.error('Error while creating');
                    deferred.reject(errResponse);
                }
            );
        return deferred.promise;
    }

    function getInfo(){
        var deferred = $q.defer();
        $http.get(REST_SERVICE_URI+'about')
            .then(
                function (response) {
                    deferred.resolve(response.data);
                },
                function (errResponse) {
                    console.error('Error while getting headers.')
                    deferred.reject(errResponse);
                }
            );
        return deferred.promise;
    }

    function sync(){
        var deferred = $q.defer();
        $http.get(REST_SERVICE_URI+'synchronize')
            .then(
                function (response) {
                    deferred.resolve(response.data);
                },
                function (errResponse) {
                    console.error('Error while synchronizing')
                    deferred.reject(errResponse);
                }
            );
        return deferred.promise;
    }
}]);
