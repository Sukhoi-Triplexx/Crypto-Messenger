import socket
import threading
import json
import time

class SecureMessengerServer:
    def __init__(self, host='localhost', port=8080):
        self.host = host
        self.port = port
        self.clients = {}  # {socket: username}
        self.client_addresses = {}  # {socket: address}
        
    def start(self):
        server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        server_socket.bind((self.host, self.port))
        server_socket.listen(5)
        
        print(f"Secure Messenger Server started on {self.host}:{self.port}")
        
        try:
            while True:
                client_socket, address = server_socket.accept()
                print(f"New connection from {address}")
                
                # Start a new thread to handle the client
                client_thread = threading.Thread(
                    target=self.handle_client, 
                    args=(client_socket, address)
                )
                client_thread.daemon = True
                client_thread.start()
                
        except KeyboardInterrupt:
            print("\nShutting down server...")
        finally:
            server_socket.close()
    
    def handle_client(self, client_socket, address):
        try:
            # Store the client connection
            self.client_addresses[client_socket] = address
            print(f"[DEBUG] Starting to handle client {address}")
            
            # Создаём построчный reader для чтения сообщений
            client_file = client_socket.makefile('r', encoding='utf-8', buffering=1)
            
            while True:
                # Читаем сообщение построчно (как отправляет клиент через println)
                line = client_file.readline()
                
                # Проверяем конец соединения
                if not line:
                    print(f"[DEBUG] Connection closed from {address}")
                    break
                
                line = line.strip()
                
                # Пропускаем пустые строки
                if not line:
                    continue
                
                print(f"[DEBUG] Received from {address}: {line}")
                    
                try:
                    message = json.loads(line)
                    print(f"[DEBUG] Parsed message: {message}")
                    self.process_message(client_socket, message)
                except json.JSONDecodeError as e:
                    print(f"[ERROR] Invalid JSON received from {address}: {line}")
                    print(f"[ERROR] JSON Error: {e}")
                    
        except ConnectionResetError:
            print(f"Client {address} disconnected unexpectedly")
        except Exception as e:
            print(f"[ERROR] Error handling client {address}: {e}")
            import traceback
            traceback.print_exc()
        finally:
            # Clean up when client disconnects
            self.remove_client(client_socket)
            try:
                client_socket.close()
            except:
                pass
    
    def process_message(self, client_socket, message):
        msg_type = message.get('type')
        
        if msg_type == 'USERNAME':
            username = message.get('content')
            self.clients[client_socket] = username
            print(f"User '{username}' connected from {self.client_addresses[client_socket]}")
            
            # Notify other users about the new user
            self.broadcast_user_joined(username)
            
        elif msg_type == 'MESSAGE':
            sender = self.clients.get(client_socket, 'Unknown')
            content = message.get('content')
            recipient = message.get('recipient')
            
            print(f"[{sender}]: {content}")
            
            # Create message to broadcast
            broadcast_msg = {
                'type': 'MESSAGE',
                'content': content,
                'sender': sender,
                'timestamp': message.get('timestamp', int(time.time() * 1000))
            }
            
            # If there's a specific recipient, send only to them
            if recipient:
                self.send_to_specific_user(recipient, broadcast_msg)
            else:
                # Broadcast to all users (including sender for confirmation)
                self.broadcast_message(broadcast_msg)
    
    def broadcast_user_joined(self, username):
        message = {
            'type': 'USER_JOINED',
            'content': f'{username} joined the chat',
            'sender': username,
            'timestamp': int(time.time() * 1000)
        }
        
        self.broadcast_message(message)
    
    def broadcast_message(self, message, exclude=None):
        message_json = json.dumps(message) + '\n'  # Добавляем \n для клиента (readLine)
        print(f"[DEBUG] Broadcasting to {len(self.clients)} clients: {message_json.strip()}")
        disconnected_clients = []
        
        for client_socket in list(self.clients.keys()):
            if client_socket == exclude:
                continue
            
            username = self.clients.get(client_socket, 'Unknown')
            try:
                print(f"[DEBUG] Sending to {username}...")
                client_socket.sendall(message_json.encode('utf-8'))
                print(f"[DEBUG] Sent to {username} successfully")
            except Exception as e:
                print(f"[ERROR] Failed to send to {username}: {e}")
                disconnected_clients.append(client_socket)
        
        # Remove disconnected clients
        for client_socket in disconnected_clients:
            self.remove_client(client_socket)
    
    def send_to_specific_user(self, username, message):
        message_json = json.dumps(message) + '\n'  # Добавляем \n
        for client_socket, client_username in self.clients.items():
            if client_username == username:
                try:
                    client_socket.send(message_json.encode('utf-8'))
                    return True
                except:
                    self.remove_client(client_socket)
                    return False
        return False
    
    def remove_client(self, client_socket):
        if client_socket in self.clients:
            username = self.clients[client_socket]
            del self.clients[client_socket]
            del self.client_addresses[client_socket]
            print(f"User '{username}' disconnected")

if __name__ == "__main__":
    server = SecureMessengerServer('0.0.0.0', 8080)
    server.start()