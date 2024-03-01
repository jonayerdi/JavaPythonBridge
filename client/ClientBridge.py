import socket
from typing import Any, Iterable

DEFAULT_ADDRESS = '127.0.0.1'
DEFAULT_PORT = 1337

def bool_from_java(x: str) -> bool:
    return {
        'true': True,
        'false': False,
    }[x]

def bool_to_java(x) -> str:
    return {
        bool: lambda v: 'true' if v else 'false'
    }.get(type(x), lambda v: 'true' if v > 0 else 'false')(x)

class ClientBridge:
    def __init__(self, address=DEFAULT_ADDRESS, port=DEFAULT_PORT):
        self._address = address
        self._port = port
        self._socket = None
    def __enter__(self): 
        return self._connect()
    def __exit__(self, type, value, traceback): 
        self._disconnect()
    def _connect(self):
        self._socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        print(f'[ClientBridge] Connecting: {self._address}:{self._port}')
        self._socket.connect((self._address, self._port))
        print(f'[ClientBridge] Ready: {self._address}:{self._port}')
        return self
    def _disconnect(self):
        print(f'[ClientBridge] Disconnecting: {self._address}:{self._port}')
        self._socket.close()
        return self
    def _call_fn(self, name: str, params: Iterable[Any], retval=lambda x: x):
        # Construct messange
        message = f"{name};{';'.join(map(str, params))}\n".encode('utf-8')
        # Send message
        self._socket.sendall(message)
        # Receive response
        data = bytes()
        while True:
            data += self._socket.recv(512)
            if data[-1] == b'\n'[-1]:
                break
        # Decode UTF-8 and return
        return retval(bytes.decode(data, encoding="utf-8").strip())
    def nextprime(self, x) -> int:
        return self._call_fn(name='nextPrime', params=[int(x)], retval=int)
    def isprime(self, x) -> bool:
        return self._call_fn(name='isPrime', params=[int(x)], retval=bool_from_java)
    def gcd(self, x) -> int:
        return self._call_fn(name='gcd', params=map(int, x), retval=int)
    def pow(self, x) -> int:
        return self._call_fn(name='pow', params=map(int, x), retval=int)
    def stirling(self, x) -> int:
        return self._call_fn(name='stirling', params=map(int, x), retval=int)
    def acos(self, x) -> float:
        return self._call_fn(name='acos', params=[float(x)], retval=float)
    def log10(self, x) -> float:
        return self._call_fn(name='log10', params=[float(x)], retval=float)
    def sin(self, x) -> float:
        return self._call_fn(name='sin', params=[float(x)], retval=float)
    def sinh(self, x) -> float:
        return self._call_fn(name='sinh', params=[float(x)], retval=float)
    def tan(self, x) -> float:
        return self._call_fn(name='tan', params=[float(x)], retval=float)
    def issorted(self, x) -> bool:
        return self._call_fn(name='isSorted', params=map(int, x), retval=bool_from_java)
    def indexof(self, x, sizearray: int) -> bool: # indexOf(int sizearray, bool[] array, bool[] target)
        return self._call_fn(name='indexOf', params=(sizearray, *map(bool_to_java, x)), retval=int)
    def meanof(self, x) -> float:
        return self._call_fn(name='meanOf', params=map(int, x), retval=float)
    def min(self, x) -> int:
        return self._call_fn(name='min', params=map(int, x), retval=int)
    def sort(self, x) -> list: # sort(int fromIndex, int toIndex, int[] array)
        return self._call_fn(name='sort', params=map(int, x), retval=lambda v: [int(x) for x in v.split(';')])

if __name__ == '__main__':
    from argparse import ArgumentParser
    parser = ArgumentParser(description='Misbehaviour detection model training')
    parser.add_argument('-a', help='remote address', dest='address', type=str, default=DEFAULT_ADDRESS, required=False)
    parser.add_argument('-p', help='remote port', dest='port', type=int, default=DEFAULT_PORT, required=False)
    params = parser.parse_args()
    
    with ClientBridge(address=params.address, port=params.port) as lib:
        def call(fn, x):
            print('{}({}) -> {}'.format(fn, x, eval(f'lib.{fn}({x})')))
        # Tests
        call('nextprime', 4)
        call('nextprime', 9)
        call('isprime', 4)
        call('isprime', 5)
        call('gcd', (4, 6))
        call('pow', (2, 3))
        call('stirling', (3, 4))
        call('acos', 0.0)
        call('log10', 2.0)
        call('sin', 1.0)
        call('sinh', 1.0)
        call('tan', 1.0)
        call('issorted', [1, 2, 4, 8, 9])
        call('issorted', [1, 2, 4, 8, 3])
        call('indexof', 'x=[True, False, False, False, True, False, False, False], sizearray=5')
        call('indexof', 'x=[True, False, True, False, True, False, False, False], sizearray=5')
        call('meanof', [10, 10, 20, 20, 30])
        call('min', [20, 10, 20, 20, 30])
        call('sort', [0, 5, 5, 4, 3, 2, 1])
        call('sort', [1, 4, 5, 4, 3, 2, 1])
