const Reserved_Space = 18;
const Custom_Link_Title = 'iLink';

const log = (...obj) => {
  console.log('--------------');
  obj.forEach(obj => console.log(JSON.stringify(obj)));
  console.log('--------------');
};

const appendStyleSheetLink = href => {
  const head = document.head || document.getElementsByTagName('head')[0];
  const link = document.createElement('link');
  link.href = href;
  link.title = Custom_Link_Title;
  link.rel = 'stylesheet';
  link.type = 'text/css';
  head.appendChild(link);
};

const onImagesLoad = () => {
  const promises = Array.prototype.slice.call(document.querySelectorAll('img')).map(
    node =>
      new Promise(res => {
        let loadImg = new Image();
        loadImg.src = node.src;
        loadImg.onload = () => res();
      })
  );

  return Promise.all(promises);
};

const onWindowLoad = (function () {
  const Time_Max_Wait = 10000;
  const Flag_All_Ready = 0b11;
  const Flag_Css_Ready = 1 << 0;
  const Flag_Image_Ready = 1 << 1;

  let callbacks = [];
  let readyStatus = 0b00;

  function setReadyStatus(status, callback) {
    if (callback) {
      callbacks.push(callback);
    }

    readyStatus |= status;

    if (readyStatus == Flag_All_Ready) {
      callbacks.forEach(res => res(true));
      callbacks = [];
    }
  }

  /** set max wait time */
  setTimeout(() => setReadyStatus(Flag_All_Ready), Time_Max_Wait);

  /** load css */
  const iStyleSheetNum = 3;

   appendStyleSheetLink('epub://normalize.css');
   appendStyleSheetLink('epub://pager.css');
   appendStyleSheetLink('epub://theme.css');

  const timer = setInterval(() => {
    let counter = 0;
    Array.prototype.slice
      .call(document.styleSheets)
      .forEach(sheet => (counter += sheet.title == Custom_Link_Title ? 1 : 0));
    if (counter == iStyleSheetNum) {
      clearInterval(timer);
      setReadyStatus(Flag_Css_Ready);
    }
  }, 50);

  /** load img */
  if (window.document.readyState !== 'complete') {
    window.onload = () => {
      onImagesLoad().then(_ => setReadyStatus(Flag_Image_Ready));
    };
  } else {
    onImagesLoad().then(_ => setReadyStatus(Flag_Image_Ready));
  }

  return () => new Promise(res => setReadyStatus(0, res));
})();

const _adjustElements = (elements, viewport) =>
  Array.prototype.slice.call(elements).forEach(ele => {
    const headerPosition = Math.floor(ele.offsetTop / viewport.height);
    const tailPosition = Math.floor((ele.offsetTop + ele.offsetHeight) / viewport.height);
    if (headerPosition != tailPosition) {
      ele.style.marginTop = viewport.height - (ele.offsetTop % viewport.height);
    }
  });

const adjustAllImages = viewport => _adjustElements(document.querySelectorAll('img'), viewport);

const adjustAllHeadlines = viewport => _adjustElements(document.querySelectorAll('h1, h2, h3, h4, h5, h6'), viewport);

const clearMask = () => {
  const topMask = document.createElement('div');
  const bottomMask = document.createElement('div');

  topMask.height = 0;
  bottomMask.height = 0;
};

async function main() {
  const content = { height: 0, pageSize: 0, currentPageIdx: 0 };
  const viewport = { width: 0, height: 0 };

  await onWindowLoad();

  const { body } = window.document;
  viewport.width = body.offsetWidth;
  viewport.height = body.offsetHeight - 2 * Reserved_Space;

  adjustAllImages(viewport);
  adjustAllHeadlines(viewport);

  content.height = body.scrollHeight;
  content.pageSize = Math.ceil(content.height / viewport.height);

  const topMask = document.createElement('div');
  const bottomMask = document.createElement('div');

  topMask.id = 'mask-top';
  bottomMask.id = 'mask-bottom';
  topMask.className = 'mask-white-top';
  bottomMask.className = 'mask-white-bottom';

  body.append(topMask, bottomMask);

  window.gotoPageByProgress = (progress = 0) => {
    console.log('[gotoPageByProgress]', progress);

    window.Epub?.onReadyStatusChange(false);
    clearMask();
    content.currentPageIdx = Math.floor((content.height * Number(progress)) / viewport.height);

    body.scrollTop = content.currentPageIdx * viewport.height + Reserved_Space;
    setTimeout(() => {
      window.Epub?.onPageViewChange(viewport.height, Reserved_Space);
    }, 50);
    window.Epub?.onPagerChange(content.pageSize, content.currentPageIdx);
  };

  window.gotoPageByIdx = (idx = 0) => {
    console.log('[gotoPageByIdx]', idx);

    window.Epub?.onReadyStatusChange(false);
    clearMask();
    body.scrollTop = idx * viewport.height + Reserved_Space;
    content.currentPageIdx = idx;
    setTimeout(() => {
      window.Epub?.onPageViewChange(viewport.height, Reserved_Space);
    }, 50);
    window.Epub?.onPagerChange(content.pageSize, content.currentPageIdx);
  };

  window.setMask = (scale = 1, top = Reserved_Space, bottom = Reserved_Space) => {
    console.log('[setMask]', top / scale, bottom / scale);

    const topMask = document.getElementById('mask-top');
    const bottomMask = document.getElementById('mask-bottom');

    topMask.style.height = `${top / scale}px`;
    bottomMask.style.height = `${bottom / scale}px`;

    window.Epub?.onReadyStatusChange(true);
  };

  body.addEventListener('click', () => {
    const nextPageIdx = prompt('跳转到');
    goToPage(nextPageIdx);
  });

  log(viewport, content);
  window.Epub?.onLoad();
}

main();
