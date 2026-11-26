(function () {
    const collapsedKey = 'todo-sidebar-collapsed';
    const language = document.documentElement.lang === 'en' ? 'en' : 'ja';
    const labels = {
        ja: {
            quickAdd: 'クイックタスク追加', inbox: '受信トレイ', today: '今日', tomorrow: '明日', week: '今週',
            upcoming: '今後の予定', overdue: '期限切れ', all: 'すべてのタスク', completed: '完了済み', projects: 'プロジェクト',
            calendar: 'カレンダー', burndown: 'バーンダウンチャート', productivity: '生産性分析', deleted: '削除済みタスク', settings: '設定',
            collapse: 'サイドバーを折りたたむ', expand: 'サイドバーを開く', menu: 'メニュー'
        },
        en: {
            quickAdd: 'Quick task add', inbox: 'Inbox', today: 'Today', tomorrow: 'Tomorrow', week: 'This week',
            upcoming: 'Upcoming', overdue: 'Overdue', all: 'All tasks', completed: 'Completed', projects: 'Projects',
            calendar: 'Calendar', burndown: 'Burndown chart', productivity: 'Productivity', deleted: 'Deleted tasks', settings: 'Settings',
            collapse: 'Collapse sidebar', expand: 'Open sidebar', menu: 'Menu'
        }
    }[language];

    const items = [
        { id: 'quickAdd', icon: '+', href: '/todos/new', action: 'quick' },
        { id: 'inbox', icon: '▣', href: '/todos?sidebar=inbox' },
        { id: 'today', icon: '☀', href: '/todos?sidebar=today' },
        { id: 'tomorrow', icon: '›', href: '/todos?sidebar=tomorrow' },
        { id: 'week', icon: '▦', href: '/todos?sidebar=week' },
        { id: 'upcoming', icon: '→', href: '/todos?sidebar=upcoming' },
        { id: 'overdue', icon: '!', href: '/todos?sidebar=overdue' },
        { id: 'all', icon: '☷', href: '/todos?sidebar=all' },
        { id: 'completed', icon: '✓', href: '/todos?sidebar=completed&showCompleted=true' },
        { id: 'projects', icon: '▤', href: '/todos' },
        { id: 'calendar', icon: '□', href: '/calendar' },
        { id: 'burndown', icon: '↘', href: '/burndown' },
        { id: 'productivity', icon: '◒', href: '/productivity' },
        { id: 'deleted', icon: '⌫', href: '/todos?trash=1' },
        { id: 'settings', icon: '⚙', href: '/settings' }
    ];

    function isActive(item) {
        const path = window.location.pathname;
        const params = new URLSearchParams(window.location.search);
        if (item.id === 'quickAdd') return path === '/todos/new';
        if (item.id === 'calendar') return path === '/calendar';
        if (item.id === 'burndown') return path === '/burndown';
        if (item.id === 'productivity') return path === '/productivity';
        if (item.id === 'settings') return path === '/settings';
        if (item.id === 'deleted') return path === '/todos' && params.get('trash') === '1';
        if (path !== '/todos') return false;
        return params.get('sidebar') === item.id || (item.id === 'inbox' && !params.get('sidebar') && params.get('trash') !== '1');
    }

    function createSidebar(counts) {
        const sidebar = document.createElement('aside');
        sidebar.className = 'app-sidebar';
        sidebar.innerHTML = '<div class="app-sidebar__header"><span class="app-sidebar__brand">ToDo</span><button class="app-sidebar__collapse" type="button"></button></div><nav class="app-sidebar__nav"></nav>';
        const nav = sidebar.querySelector('.app-sidebar__nav');

        items.forEach((item) => {
            const link = document.createElement('a');
            link.className = 'app-sidebar__item';
            if (isActive(item)) link.classList.add('is-active');
            link.href = item.href;
            link.dataset.item = item.id;
            link.title = labels[item.id];
            link.innerHTML = `<span class="app-sidebar__icon" aria-hidden="true">${item.icon}</span><span class="app-sidebar__label">${labels[item.id]}</span>`;
            const count = counts[item.id];
            if (typeof count === 'number' && count > 0) {
                link.insertAdjacentHTML('beforeend', `<span class="app-sidebar__count">${count}</span>`);
            }
            nav.appendChild(link);
        });

        const collapse = sidebar.querySelector('.app-sidebar__collapse');
        collapse.textContent = '‹';
        collapse.title = labels.collapse;
        collapse.addEventListener('click', () => {
            document.body.classList.toggle('sidebar-collapsed');
            localStorage.setItem(collapsedKey, document.body.classList.contains('sidebar-collapsed') ? 'true' : 'false');
        });
        document.body.prepend(sidebar);
        document.body.classList.add('has-sidebar');
        if (localStorage.getItem(collapsedKey) === 'true') document.body.classList.add('sidebar-collapsed');

        const mobileToggle = document.createElement('button');
        mobileToggle.className = 'app-sidebar__mobile-toggle';
        mobileToggle.type = 'button';
        mobileToggle.textContent = '☰';
        mobileToggle.title = labels.menu;
        mobileToggle.addEventListener('click', () => sidebar.classList.toggle('is-open'));
        document.body.appendChild(mobileToggle);
    }

    fetch('/api/sidebar/counts')
        .then((response) => response.ok ? response.json() : {})
        .catch(() => ({}))
        .then((counts) => createSidebar(counts));
})();
