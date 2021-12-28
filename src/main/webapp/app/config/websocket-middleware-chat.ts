import SockJS from 'sockjs-client';

import Stomp, { Client, Subscription } from 'webstomp-client';
import { Observable } from 'rxjs';
import { Storage } from 'react-jhipster';
import { IMessage } from 'app/shared/model/message.model';

let stompClient: Client = null;

let subscriber: Subscription = null;
let connection: Promise<any>;
let connectedPromise: any = null;
let listener: Observable<IMessage>;
let listenerObserver: any;
let alreadyConnectedOnce = false;

const createConnection = (): Promise<any> => new Promise(resolve => (connectedPromise = resolve));

const createListener = (): Observable<any> =>
  new Observable(observer => {
    listenerObserver = observer;
  });

export const sendMessageWebSocket = (message: IMessage) => {
  connection?.then(() => {
    stompClient?.send(
      `/chat/${message.chat.id}/sent`, // destination
      JSON.stringify(message)
    );
  });
};

const subscribe = (chatId: string) => {
  connection.then(() => {
    subscriber = stompClient.subscribe(`/chat/${chatId}`, data => {
      const message = JSON.parse(data.body) as IMessage;
      if (message.id) {
        listenerObserver.next(message);
      }
    });
  });
};

const connect = () => {
  if (connectedPromise !== null || alreadyConnectedOnce) {
    // the connection is already being established
    return;
  }
  connection = createConnection();
  listener = createListener();

  // building absolute path so that websocket doesn't fail when deploying with a context path
  const loc = window.location;
  const baseHref = document.querySelector('base').getAttribute('href').replace(/\/$/, '');

  const headers = {};
  let url = '//' + loc.host + baseHref + '/websocket/chat';
  const authToken = Storage.local.get('jhi-authenticationToken') || Storage.session.get('jhi-authenticationToken');
  if (authToken) {
    url += '?access_token=' + authToken;
  }
  const socket = new SockJS(url);
  stompClient = Stomp.over(socket, { protocols: ['v12.stomp'] });

  stompClient.connect(headers, () => {
    connectedPromise('success');
    connectedPromise = null;
    alreadyConnectedOnce = true;
  });
};

const disconnect = (chatId: string) => {
  if (stompClient !== null) {
    if (stompClient.connected) {
      stompClient.disconnect(null, { 'chat-id': chatId });
    }
    stompClient = null;
  }
  alreadyConnectedOnce = false;
};

export const receiver: () => Observable<IMessage> = () => listener;

const unsubscribe = () => {
  if (subscriber !== null) {
    subscriber.unsubscribe();
  }
  listener = createListener();
};

export const initChatWebSocket = (chatId: string) => {
  connect();
  if (!alreadyConnectedOnce) {
    return new Promise<string>((resolve, reject) => {
      /*    receiver().subscribe(message => {
            return dispatch(websocketChatMessage(message));
          });*/
      try {
        subscribe(chatId);
        resolve('SUBSCRIPTION AU CHAT OK');
      } catch (e) {
        reject('ECHEC DE lA CONNEXION AU CHAT');
      }
    });
  }
};

export const leaveChatWebSocket = (chatId: string) => {
  return new Promise<string>(resolve => {
    /*    receiver().subscribe(message => {
          return dispatch(websocketChatMessage(message));
        });*/
    unsubscribe();
    disconnect(chatId);
    resolve('OK pour la deconnexion');
  });
};
