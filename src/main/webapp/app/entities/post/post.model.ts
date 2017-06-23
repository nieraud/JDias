
const enum PostType {
    'STATUSMESSAGE',
    'RESHARE'

};
import { StatusMessage } from '../status-message';
import { Reshare } from '../reshare';
import { Comment } from '../comment';
import { AspectVisiblity } from '../aspect-visiblity';
import { Like } from '../like';
import { Tag } from '../tag';
import { Person } from '../person';
export class Post {
    constructor(
        public id?: number,
        public author?: string,
        public text?: string,
        public guid?: string,
        public createdAt?: any,
        public pub?: boolean,
        public providerDisplayName?: string,
        public postType?: PostType,
        public statusMessage?: StatusMessage,
        public reshare?: Reshare,
        public comments?: Comment,
        public aspectVisiblities?: AspectVisiblity,
        public likes?: Like,
        public tags?: Tag,
        public person?: Person,
    ) {
        this.pub = false;
    }
}
