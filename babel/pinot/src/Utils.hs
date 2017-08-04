{-# LANGUAGE FlexibleInstances #-}
module Utils where
import Data.Default
import Data.Text (Text, unpack)
import qualified Data.Text as T
import Data.Traversable (mapAccumL)
import Data.Aeson
import Data.Aeson.Types (Pair, Series)
import Data.Monoid ((<>))
import Data.Maybe (catMaybes)
import Control.Applicative ((<|>))
import qualified Data.ByteString.Lazy as B
import qualified Data.HashMap.Lazy as H
-- import qualified Text.Pandoc.Builder as P
import Control.Lens hiding ((.=))
import qualified Notebook as N

import Data.UUID (UUID)
import qualified Data.UUID as UUID

import System.FilePath ((</>), takeDirectory, dropExtension)
import System.Directory (createDirectoryIfMissing, doesDirectoryExist)

objectMaybe :: [Maybe Pair] -> Value
objectMaybe = object . catMaybes

infixr 8 .=?
class MaybeKeyValue kv where
  (.=?) :: ToJSON v => Text -> Maybe v -> kv

instance MaybeKeyValue Series where
  k .=? Nothing  = mempty
  k .=? (Just v) = k .= v

instance MaybeKeyValue (Maybe Pair) where
  k .=? Nothing = Nothing
  k .=? (Just v) = Just (k .= v)

instance KeyValue (Maybe Pair) where
  k .= v = Just (k .= v)

concatMapM :: (Traversable t, Monad m) => (a -> m [b]) -> t a -> m [b]
concatMapM f xs = concat <$> mapM f xs

ensureCanBeCreated :: FilePath -> IO ()
ensureCanBeCreated f =
  createDirectoryIfMissing True (takeDirectory f)

swapExtension :: String -> FilePath -> FilePath
swapExtension to f = (dropExtension f) ++ to

-- instance ToJSON UUID where
--   toJSON uid = toJSON (UUID.toString uid)
--   toEncoding uid = toEncoding (UUID.toString uid)

-- instance FromJSON UUID where
--   parseJSON js = UUID.fromString (parseJSON js)

instance Default UUID where
  def = UUID.nil
