
function testAnonFunc() returns string {
    function (string, string) returns (string) anonFunction =
                function (string x, string y) returns (string) {
                    return x + y;
                };
    return anonFunction.call("Hello ", "World.!!!");
}

function testFPPassing() returns int {
    function (int, string)  anonFunction =
                    function (int x, string y) {
                        string z = y + y;
                    };
    var fp = useFp(anonFunction);
    return fp.call(10, 20);
}

function useFp(function (int, string) fp) returns (function (int, int) returns (int)) {
    fp.call(10, "y");
    function (int, int) returns (int) fp2 =
                        function (int x, int y) returns (int) {
                            return x * y;
                        };
    return fp2;
}

function testBasicClosure() returns int {
    var foo = basicClosure();
    return foo.call(3);

}

function basicClosure() returns (function (int) returns int) {
    int a = 3;
    var foo = function (int b) returns int {
        int c = 34;
        if (b == 3) {
            c = c + b + a + 5;
        }
        return c + a;
    };

    a = 100;
    return foo;
}

function testMultilevelClosure() returns int {
     var bar = multilevelClosure();
     return bar.call(5);
}

function multilevelClosure() returns (function (int) returns int) {
    int a = 2;
    var func1 = function (int x) returns int {
        int b = 23;

        a = a + 8;
        var func2 = function (int y) returns int {
            int c = 7;
            var func3 = function (int z) returns int {

                b = b + 1;
                return x + y + z + a + b + c;
            };
            return func3.call(8) + y + x;
        };
        return func2.call(4) + x;
    };
    return func1;
}
