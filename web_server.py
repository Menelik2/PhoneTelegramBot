import http.server
import socketserver
import os
import sys

PORT = 3000
DIRECTORY = os.path.join(os.path.dirname(os.path.abspath(__file__)), "web")

class ThreadingHTTPServer(socketserver.ThreadingMixIn, http.server.HTTPServer):
    pass

class MyHandler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=DIRECTORY, **kwargs)

    def log_message(self, format, *args):
        # Override log to print to stdout clearly
        sys.stdout.write(f"[Web Server] {format % args}\n")
        sys.stdout.flush()

if __name__ == "__main__":
    if not os.path.exists(DIRECTORY):
        os.makedirs(DIRECTORY, exist_ok=True)
        print(f"Created web directory at: {DIRECTORY}")

    print(f"Starting background web server on port {PORT}...")
    try:
        # Enable address reuse so we don't block the port on restart
        socketserver.TCPServer.allow_reuse_address = True
        with ThreadingHTTPServer(("", PORT), MyHandler) as httpd:
            print(f"Web server successfully listening on http://localhost:{PORT}")
            print(f"Serving files from directory: {DIRECTORY}")
            sys.stdout.flush()
            httpd.serve_forever()
    except Exception as e:
        print(f"Fatal error starting web server: {e}", file=sys.stderr)
        sys.exit(1)
